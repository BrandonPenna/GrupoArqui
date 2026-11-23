package com.ticketya.catalogo.service;

import com.ticketya.catalogo.client.VentasClient;
import com.ticketya.catalogo.config.CacheConfig;
import com.ticketya.catalogo.dto.EventoCreateDTO;
import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.*;
import com.ticketya.catalogo.repository.EventoRepository;
import com.ticketya.catalogo.repository.LugarRepository;
import com.ticketya.catalogo.repository.SectorRepository;
import com.ticketya.catalogo.specification.EventoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final SectorRepository sectorRepository;
    private final VentasClient ventasClient;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EVENTOS_DETALLE, allEntries = true)
    public Evento crearEventoBorrador(EventoCreateDTO dto, String organizadorId) {
        Lugar lugar = lugarRepository.findById(dto.getLugarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado con ID: " + dto.getLugarId()));

        Evento evento = Evento.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(dto.getTipo())
                .organizadorId(organizadorId)
                .participantes(dto.getParticipantes())
                .fechaHora(dto.getFechaHora())
                .lugar(lugar)
                .estado(EstadoEvento.BORRADOR)
                .altaDemanda(Boolean.TRUE.equals(dto.getAltaDemanda()))
                .build();

        if (dto.getPrecios() != null) {
            for (var pDto : dto.getPrecios()) {
                Sector sector = sectorRepository.findById(pDto.getSectorId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector no encontrado con ID: " + pDto.getSectorId()));

                EventoPrecioSector eps = EventoPrecioSector.builder()
                        .evento(evento)
                        .sector(sector)
                        .precio(pDto.getPrecio())
                        .build();

                evento.getPreciosSectores().add(eps);
            }
        }

        evento.recalcularPrecioDesde();
        return eventoRepository.save(evento);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EVENTOS_DETALLE, allEntries = true)
    public Evento publicarEvento(Long eventoId, String organizadorId) {
        Evento evento = buscarPorId(eventoId);

        if (!evento.getOrganizadorId().equals(organizadorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el organizador creador puede publicar este evento");
        }
        if (evento.getPreciosSectores().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede publicar un evento sin precios definidos por sector");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        Evento guardado = eventoRepository.save(evento);

        if (notificacionService != null) {
            notificacionService.notificarPublicacionArtistaFavorito(guardado);
        }

        return guardado;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EVENTOS_DETALLE, allEntries = true)
    public Evento cancelarEvento(Long eventoId, String userId, String role) {
        Evento evento = buscarPorId(eventoId);

        if ("ORGANIZADOR".equalsIgnoreCase(role) && !evento.getOrganizadorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el organizador creador puede cancelar este evento");
        }
        if (evento.getEstado() == EstadoEvento.CANCELADO || evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento ya está " + evento.getEstado());
        }

        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            ventasClient.procesarCancelacion(eventoId);
        }

        evento.setEstado(EstadoEvento.CANCELADO);
        Evento cancelado = eventoRepository.save(evento);
        log.info("[OPERACION_CRITICA] tipo=CANCELACION_EVENTO usuario={} rol={} eventoId={}", userId, role, eventoId);
        return cancelado;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EVENTOS_DETALLE, allEntries = true)
    public Evento suspenderEvento(Long eventoId) {
        Evento evento = buscarPorId(eventoId);

        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.AGOTADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede suspender un evento publicado o agotado");
        }

        evento.setEstado(EstadoEvento.SUSPENDIDO);
        return eventoRepository.save(evento);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EVENTOS_DETALLE, allEntries = true)
    public Evento reactivarEvento(Long eventoId) {
        Evento evento = buscarPorId(eventoId);

        if (evento.getEstado() != EstadoEvento.SUSPENDIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede reactivar un evento suspendido");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        return eventoRepository.save(evento);
    }

    public Page<Evento> buscarEventos(EventoFiltroDTO filtro, String orden, Pageable pageable) {
        if ("relevancia".equalsIgnoreCase(orden)) {
            Pageable sinOrden = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return eventoRepository.findAll(
                    EventoSpecification.conFiltros(filtro)
                            .and(EventoSpecification.ordenadoPorRelevancia(filtro != null ? filtro.getQ() : null)),
                    sinOrden);
        }
        return eventoRepository.findAll(EventoSpecification.conFiltros(filtro), pageable);
    }

    @Cacheable(value = CacheConfig.CACHE_EVENTOS_DETALLE, key = "#id")
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado con ID: " + id));
    }
}
