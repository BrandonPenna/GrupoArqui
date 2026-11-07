package com.ticketya.ventas_service.controller;

import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.model.Compra;
import com.ticketya.ventas_service.model.Reserva;
import com.ticketya.ventas_service.service.VentasServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VentasController {

    private final VentasServices ventasService;

    // 1. Crear reserva temporal (10 min) - POST /api/reservas
    @PostMapping("/reservas")
    public ResponseEntity<Reserva> crearReserva(
            @RequestBody ReservaCreateDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        Reserva reserva = ventasService.crearReservaTemporal(dto, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    // 2. Consultar reservas del cliente - GET /api/reservas
    @GetMapping("/reservas")
    public ResponseEntity<List<Reserva>> misReservas(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(ventasService.listarReservasCliente(clienteId));
    }

    // 3. Confirmar compra y emitir QR - POST /api/compras/confirmar
    @PostMapping("/compras/confirmar")
    public ResponseEntity<Compra> confirmarCompra(
            @RequestBody ConfirmarCompraDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        Compra compra = ventasService.confirmarCompra(dto, clienteId);
        return ResponseEntity.ok(compra);
    }

    // 4. Consultar compras y entradas con QR del cliente - GET /api/compras
    @GetMapping("/compras")
    public ResponseEntity<List<Compra>> misCompras(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(ventasService.listarComprasCliente(clienteId));
    }
}