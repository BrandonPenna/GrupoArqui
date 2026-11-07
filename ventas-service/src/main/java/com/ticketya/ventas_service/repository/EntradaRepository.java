package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.Entrada;
import com.ticketya.ventas_service.model.EstadoEntrada;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Bloqueo pesimista para evitar doble venta de un mismo asiento numerado
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Entrada e WHERE e.eventoId = :eventoId AND e.sectorId = :sectorId " +
            "AND e.fila = :fila AND e.asiento = :asiento AND e.estado IN :estadosActivos")
    List<Entrada> findAsientoOcupadoConLock(
            @Param("eventoId") Long eventoId,
            @Param("sectorId") Long sectorId,
            @Param("fila") Integer fila,
            @Param("asiento") Integer asiento,
            @Param("estadosActivos") Collection<EstadoEntrada> estadosActivos
    );

    // Bloqueo pesimista para contar y bloquear las entradas activas de un sector general (no numerado)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Entrada e WHERE e.eventoId = :eventoId AND e.sectorId = :sectorId " +
            "AND e.estado IN :estadosActivos")
    List<Entrada> findEntradasSectorConLock(
            @Param("eventoId") Long eventoId,
            @Param("sectorId") Long sectorId,
            @Param("estadosActivos") Collection<EstadoEntrada> estadosActivos
    );

    // Para limitar la cantidad máxima de entradas por cliente para un mismo evento (Req. 4)
    @Query("SELECT COUNT(e) FROM Entrada e WHERE e.eventoId = :eventoId " +
            "AND e.reserva.clienteId = :clienteId AND e.estado IN :estadosActivos")
    long countEntradasActivasClienteEnEvento(
            @Param("eventoId") Long eventoId,
            @Param("clienteId") String clienteId,
            @Param("estadosActivos") Collection<EstadoEntrada> estadosActivos
    );

    List<Entrada> findByEventoIdAndEstado(Long eventoId, EstadoEntrada estado);
}