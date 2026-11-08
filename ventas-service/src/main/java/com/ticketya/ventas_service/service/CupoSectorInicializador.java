package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.model.CupoSector;
import com.ticketya.ventas_service.repository.CupoSectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea (una sola vez) la fila de cupo de un sector no numerado.
 * Corre en su propia transacción (REQUIRES_NEW): si dos pedidos la quieren crear a la vez,
 * la restricción UNIQUE(evento_id, sector_id) deja pasar a uno y el otro simplemente sigue,
 * porque la fila ya existe.
 */
@Component
@RequiredArgsConstructor
public class CupoSectorInicializador {

    private final CupoSectorRepository cupoSectorRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearSiNoExiste(Long eventoId, Long sectorId, Integer capacidad) {
        if (cupoSectorRepository.existsByEventoIdAndSectorId(eventoId, sectorId)) {
            return;
        }
        try {
            cupoSectorRepository.saveAndFlush(CupoSector.builder()
                    .eventoId(eventoId)
                    .sectorId(sectorId)
                    .capacidad(capacidad)
                    .ocupados(0)
                    .build());
        } catch (DataIntegrityViolationException yaExiste) {
            // Otro pedido la creó al mismo tiempo: no es un error.
        }
    }
}
