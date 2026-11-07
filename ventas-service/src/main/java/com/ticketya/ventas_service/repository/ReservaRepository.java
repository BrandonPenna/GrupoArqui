package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.EstadoReserva;
import com.ticketya.ventas_service.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Para la tarea programada (@Scheduled) que vence reservas de más de 10 minutos
    List<Reserva> findByEstadoAndFechaExpiracionBefore(EstadoReserva estado, LocalDateTime fechaLimite);

    List<Reserva> findByClienteId(String clienteId);
}