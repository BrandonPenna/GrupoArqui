package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.Devolucion;
import com.ticketya.ventas_service.model.EstadoDevolucion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    List<Devolucion> findByEstado(EstadoDevolucion estado);

    boolean existsByEntrada_IdAndEstadoIn(
        Long entradaId,
        List<EstadoDevolucion> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Devolucion d WHERE d.id = :id")
    Optional<Devolucion> findByIdForUpdate(@Param("id") Long id);
}