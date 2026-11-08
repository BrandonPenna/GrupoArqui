package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.EstadoReserva;
import com.ticketya.ventas_service.model.Reserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Para la tarea programada (@Scheduled) que vence reservas de más de 10 minutos
    List<Reserva> findByEstadoAndFechaExpiracionBefore(EstadoReserva estado, LocalDateTime fechaLimite);

    List<Reserva> findByClienteId(String clienteId);

    // Bloquea la reserva al confirmar: dos "Pagar" simultáneos de la misma reserva se atienden de a uno
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r WHERE r.id = :id")
    Optional<Reserva> findByIdForUpdate(@Param("id") Long id);
}