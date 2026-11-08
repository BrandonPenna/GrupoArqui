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
import com.ticketya.ventas_service.dto.SolicitudDevolucionDTO;
import com.ticketya.ventas_service.dto.ResolverDevolucionDTO;
import com.ticketya.ventas_service.model.Devolucion;

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

    // 5. Solicitar devolución de una entrada vendida con al menos 72 horas de anticipación - POST /api/devoluciones/entradas/{entradaId}
    @PostMapping("/devoluciones/entradas/{entradaId}")
    public ResponseEntity<Devolucion> solicitarDevolucion(
            @PathVariable Long entradaId,
            @RequestBody SolicitudDevolucionDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        
        Devolucion devolucion = ventasService.solicitarDevolucion(entradaId, clienteId, dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucion);

    }

    // 6. Un ADMIN resuelve solicitud de devolución (aprobar o rechazar) - PUT /api/devoluciones/{devolucionId}/resolver
    @PutMapping("/devoluciones/{devolucionId}/resolver")
    public ResponseEntity<Devolucion> resolverDevolucion(
        @PathVariable Long devolucionId,
        @RequestBody ResolverDevolucionDTO dto,
        @RequestHeader("X-User-Role") String rol
    ){
        if(!"ADMIN".equals(rol)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(dto.getAprobar() == null){
            return ResponseEntity.badRequest().build();
        }

        Devolucion devolucion = ventasService.resolverDevolucion(devolucionId, dto.getAprobar());
        return ResponseEntity.ok(devolucion);
    }
}