package com.ticketya.ventas_service.controller;

import com.ticketya.ventas_service.model.Compra;
import com.ticketya.ventas_service.repository.CompraRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Endpoint interno para alimentar el motor de recomendaciones de catalogo-service
 * según el historial de compras ("quienes compraron entradas para este evento también compraron para...") (Req. 10).
 */
@Hidden
@RestController
@RequestMapping("/internal/recomendaciones")
@RequiredArgsConstructor
public class RecomendacionInternaController {

    private final CompraRepository compraRepository;

    @GetMapping("/co-compras/{clienteId}")
    public ResponseEntity<List<Long>> obtenerEventosCoComprados(@PathVariable String clienteId) {
        List<Compra> todas = compraRepository.findAll();

        Set<Long> misEventos = todas.stream()
                .filter(c -> clienteId.equals(c.getClienteId()))
                .map(Compra::getEventoId)
                .collect(Collectors.toSet());

        if (misEventos.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Clientes que compraron para alguno de los mismos eventos que este cliente
        Set<String> clientesSimilares = todas.stream()
                .filter(c -> !clienteId.equals(c.getClienteId()) && misEventos.contains(c.getEventoId()))
                .map(Compra::getClienteId)
                .collect(Collectors.toSet());

        // Otros eventos que esos clientes similares también compraron
        Set<Long> sugeridosPorHistorial = new LinkedHashSet<>();
        for (Compra c : todas) {
            if (clientesSimilares.contains(c.getClienteId()) && !misEventos.contains(c.getEventoId())) {
                sugeridosPorHistorial.add(c.getEventoId());
            }
        }

        return ResponseEntity.ok(new ArrayList<>(sugeridosPorHistorial));
    }
}
