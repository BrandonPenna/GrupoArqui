package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByClienteId(String clienteId);

    Optional<Compra> findByReservaId(Long reservaId);
}