package com.ticketya.catalogo.specification;

import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.EstadoEvento;
import com.ticketya.catalogo.model.Evento;
import jakarta.persistence.criteria.*;
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

            if (tieneTexto(filtro.getQ())) {
                String patron = "%" + filtro.getQ().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), patron),
                        cb.like(cb.lower(root.get("descripcion")), patron),
                        existeParticipanteParecido(root, query, cb, patron)));
            }

            // 1. Tipo de Evento
            if (filtro.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filtro.getTipo()));
            }

            // 2. Organizador
            if (tieneTexto(filtro.getOrganizadorId())) {
                predicates.add(cb.equal(root.get("organizadorId"), filtro.getOrganizadorId()));
            }

            // 3. Participante (busca dentro del @ElementCollection)
            if (tieneTexto(filtro.getParticipante())) {
                Expression<List<String>> participantes = root.get("participantes");
                predicates.add(cb.isMember(filtro.getParticipante(), participantes));
            }

            // 4. Ciudad (a través de Lugar)
            if (tieneTexto(filtro.getCiudad())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("lugar").get("ciudad")),
                        filtro.getCiudad().toLowerCase()));
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

    // Ordena por relevancia: nombre = 3, participante = 2, descripción = 1
    public static Specification<Evento> ordenadoPorRelevancia(String texto) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
                return null;
            }
            if (!tieneTexto(texto)) {
                query.orderBy(cb.asc(root.get("fechaHora")));
                return null;
            }
            String patron = "%" + texto.toLowerCase() + "%";
            Expression<Integer> enNombre = cb.<Integer>selectCase()
                    .when(cb.like(cb.lower(root.get("nombre")), patron), 3).otherwise(0);
            Expression<Integer> enParticipante = cb.<Integer>selectCase()
                    .when(existeParticipanteParecido(root, query, cb, patron), 2).otherwise(0);
            Expression<Integer> enDescripcion = cb.<Integer>selectCase()
                    .when(cb.like(cb.lower(root.get("descripcion")), patron), 1).otherwise(0);
            Expression<Integer> puntaje = cb.sum(cb.sum(enNombre, enParticipante), enDescripcion);
            query.orderBy(cb.desc(puntaje), cb.asc(root.get("fechaHora")));
            return null;
        };
    }

    private static Predicate existeParticipanteParecido(Root<Evento> root, CriteriaQuery<?> query,
                                                       CriteriaBuilder cb, String patron) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<Evento> e2 = sub.from(Evento.class);
        Join<Evento, String> p = e2.join("participantes");
        sub.select(e2.get("id")).where(
                cb.equal(e2.get("id"), root.get("id")),
                cb.like(cb.lower(p), patron));
        return cb.exists(sub);
    }

    private static boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }
}
