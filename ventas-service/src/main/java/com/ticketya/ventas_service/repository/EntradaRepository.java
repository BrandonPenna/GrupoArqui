package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.Entrada;
import com.ticketya.ventas_service.model.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Para limitar la cantidad máxima de entradas por cliente para un mismo evento (Req. 4)
    @Query("SELECT COUNT(e) FROM Entrada e WHERE e.eventoId = :eventoId " +
            "AND e.reserva.clienteId = :clienteId AND e.estado IN :estadosActivos")
    long countEntradasActivasClienteEnEvento(
            @Param("eventoId") Long eventoId,
            @Param("clienteId") String clienteId,
            @Param("estadosActivos") Collection<EstadoEntrada> estadosActivos
    );

    List<Entrada> findByEventoIdAndEstado(Long eventoId, EstadoEntrada estado);

    // Entradas que ocupan lugar en un sector (RESERVADAS o VENDIDAS)
    @Query("SELECT e FROM Entrada e WHERE e.eventoId = :eventoId AND e.sectorId = :sectorId " +
            "AND e.estado IN :estadosActivos")
    List<Entrada> findOcupadasSector(@Param("eventoId") Long eventoId,
                                     @Param("sectorId") Long sectorId,
                                     @Param("estadosActivos") Collection<EstadoEntrada> estadosActivos);
}