package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.Devolucion;
import com.ticketya.ventas_service.model.EstadoDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    List<Devolucion> findByEstado(EstadoDevolucion estado);
}