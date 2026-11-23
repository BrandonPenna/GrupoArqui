package com.ticketya.catalogo.service;

import com.ticketya.catalogo.client.VentasClient;
import com.ticketya.catalogo.dto.PreferenciaClienteDTO;
import com.ticketya.catalogo.model.EstadoEvento;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.PreferenciaClienteDocument;
import com.ticketya.catalogo.repository.EventoRepository;
import com.ticketya.catalogo.repository.PreferenciaClienteNoSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestión de preferencias de clientes en base NoSQL y motor de recomendaciones (Req. 10 / US-19 y US-20).
 */
@Service
@RequiredArgsConstructor
public class PreferenciaRecomendacionService {

    private final PreferenciaClienteNoSqlRepository preferenciaRepository;
    private final EventoRepository eventoRepository;

    @Autowired(required = false)
    private VentasClient ventasClient;

    public PreferenciaClienteDocument guardarPreferencias(String clienteId, PreferenciaClienteDTO dto) {
        PreferenciaClienteDocument doc = preferenciaRepository.findByClienteId(clienteId)
                .orElseGet(() -> PreferenciaClienteDocument.builder().clienteId(clienteId).build());

        doc.setTiposEvento(dto.getTiposEvento() != null ? dto.getTiposEvento() : new ArrayList<>());
        doc.setArtistasFavoritos(dto.getArtistasFavoritos() != null ? dto.getArtistasFavoritos() : new ArrayList<>());
        doc.setCiudad(dto.getCiudad());
        doc.setFechaActualizacion(LocalDateTime.now());

        return preferenciaRepository.save(doc);
    }

    public PreferenciaClienteDocument obtenerPreferencias(String clienteId) {
        return preferenciaRepository.findByClienteId(clienteId)
                .orElseGet(() -> PreferenciaClienteDocument.builder()
                        .clienteId(clienteId)
                        .tiposEvento(List.of())
                        .artistasFavoritos(List.of())
                        .build());
    }

    /**
     * Sugiere eventos publicados combinando:
     * 1. Historial colaborativo ("quienes compraron entradas para este evento también compraron para..."): +6 puntos
     * 2. Coincidencia con artistas favoritos (NoSQL): +5 puntos
     * 3. Coincidencia con la ciudad preferida del cliente (NoSQL): +3 puntos
     * 4. Coincidencia con tipos de evento preferidos (NoSQL): +2 puntos
     */
    @Transactional(readOnly = true)
    public List<Evento> recomendarEventos(String clienteId) {
        PreferenciaClienteDocument pref = obtenerPreferencias(clienteId);
        List<Long> coCompradosList = ventasClient != null ? ventasClient.obtenerEventosCoComprados(clienteId) : null;
        Set<Long> coComprados = coCompradosList != null ? new HashSet<>(coCompradosList) : Set.of();

        List<Evento> publicados = eventoRepository.findAll().stream()
                .filter(e -> e.getEstado() == EstadoEvento.PUBLICADO)
                .toList();

        return publicados.stream()
                .sorted(Comparator
                        .comparingInt((Evento e) -> calcularPuntajeRecomendacion(e, pref, coComprados))
                        .reversed()
                        .thenComparing(Evento::getFechaHora, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10)
                .toList();
    }

    private int calcularPuntajeRecomendacion(Evento evento, PreferenciaClienteDocument pref, Set<Long> coComprados) {
        int puntos = 0;

        if (evento.getId() != null && coComprados.contains(evento.getId())) {
            puntos += 6;
        }

        if (pref.getCiudad() != null && evento.getLugar() != null && evento.getLugar().getCiudad() != null
                && pref.getCiudad().equalsIgnoreCase(evento.getLugar().getCiudad())) {
            puntos += 3;
        }

        if (pref.getTiposEvento() != null && evento.getTipo() != null
                && pref.getTiposEvento().contains(evento.getTipo())) {
            puntos += 2;
        }

        if (pref.getArtistasFavoritos() != null && evento.getParticipantes() != null) {
            for (String fav : pref.getArtistasFavoritos()) {
                boolean match = evento.getParticipantes().stream()
                        .anyMatch(p -> p.equalsIgnoreCase(fav) || p.toLowerCase().contains(fav.toLowerCase()));
                if (match) {
                    puntos += 5;
                }
            }
        }

        return puntos;
    }
}
