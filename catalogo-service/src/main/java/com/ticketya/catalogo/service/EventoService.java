package com.ticketya.catalogo.service;

import com.ticketya.catalogo.dto.EventoCreateDTO;
import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.*;
import com.ticketya.catalogo.repository.EventoRepository;
import com.ticketya.catalogo.repository.LugarRepository;
import com.ticketya.catalogo.repository.SectorRepository;
import com.ticketya.catalogo.specification.EventoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final SectorRepository sectorRepository;

    @Transactional
    public Evento crearEventoBorrador(EventoCreateDTO dto, String organizadorId) {
        Lugar lugar = lugarRepository.findById(dto.getLugarId())
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado con ID: " + dto.getLugarId()));

        Evento evento = Evento.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(dto.getTipo())
                .organizadorId(organizadorId)
                .participantes(dto.getParticipantes())
                .fechaHora(dto.getFechaHora())
                .lugar(lugar)
                .estado(EstadoEvento.BORRADOR)
                .build();

        if (dto.getPrecios() != null) {
            for (var pDto : dto.getPrecios()) {
                Sector sector = sectorRepository.findById(pDto.getSectorId())
                        .orElseThrow(() -> new RuntimeException("Sector no encontrado con ID: " + pDto.getSectorId()));

                EventoPrecioSector eps = EventoPrecioSector.builder()
                        .evento(evento)
                        .sector(sector)
                        .precio(pDto.getPrecio())
                        .build();

                evento.getPreciosSectores().add(eps);
            }
        }

        return eventoRepository.save(evento);
    }

    @Transactional
    public Evento publicarEvento(Long eventoId, String organizadorId) {
        Evento evento = buscarPorId(eventoId);

        if (!evento.getOrganizadorId().equals(organizadorId)) {
            throw new RuntimeException("Solo el organizador creador puede publicar este evento");
        }
        if (evento.getPreciosSectores().isEmpty()) {
            throw new RuntimeException("No se puede publicar un evento sin precios definidos por sector");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        return eventoRepository.save(evento);
    }

    @Transactional
    public Evento cancelarEvento(Long eventoId) {
        Evento evento = buscarPorId(eventoId);
        evento.setEstado(EstadoEvento.CANCELADO);
        return eventoRepository.save(evento);
    }

    public Page<Evento> buscarEventos(EventoFiltroDTO filtro, Pageable pageable) {
        return eventoRepository.findAll(EventoSpecification.conFiltros(filtro), pageable);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
    }
}