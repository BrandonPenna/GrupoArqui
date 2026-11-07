package com.ticketya.catalogo.specification;

import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.EstadoEvento;
import com.ticketya.catalogo.model.Evento;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EventoSpecification {

    public static Specification<Evento> conFiltros(EventoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Por defecto en búsquedas públicas solo se muestran eventos PUBLICADOS
            predicates.add(cb.equal(root.get("estado"), EstadoEvento.PUBLICADO));

            if (filtro == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 1. Tipo de Evento
            if (filtro.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filtro.getTipo()));
            }

            // 2. Organizador
            if (filtro.getOrganizadorId() != null && !filtro.getOrganizadorId().isBlank()) {
                predicates.add(cb.equal(root.get("organizadorId"), filtro.getOrganizadorId()));
            }

            // 3. Participante (busca dentro del @ElementCollection)
            if (filtro.getParticipante() != null && !filtro.getParticipante().isBlank()) {
                Expression<List<String>> participantes = root.get("participantes");
                predicates.add(cb.isMember(filtro.getParticipante(), participantes));
            }

            // 4. Ciudad (a través de Lugar)
            if (filtro.getCiudad() != null && !filtro.getCiudad().isBlank()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("lugar").get("ciudad")), 
                    filtro.getCiudad().toLowerCase()
                ));
            }

            // 5. Rango de Fechas
            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), filtro.getFechaDesde()));
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), filtro.getFechaHasta()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}