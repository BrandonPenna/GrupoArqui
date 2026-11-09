package com.ticketya.ventas_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Ventas", description = "Reservas, compras y devoluciones")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VentasController {

    private final VentasServices ventasService;

    // 1. Crear reserva temporal (10 min) - POST /api/reservas
    @Operation(summary = "Crear reserva temporal",
            description = "Reserva los lugares por 10 minutos. Máximo 6 entradas por cliente y evento.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada en estado PENDIENTE"),
            @ApiResponse(responseCode = "400", description = "Pedido inválido o supera el máximo de entradas por cliente"),
            @ApiResponse(responseCode = "409", description = "El lugar ya está ocupado, no hay capacidad o el evento no está publicado")
    })
    @PostMapping("/reservas")
    public ResponseEntity<Reserva> crearReserva(
            @RequestBody ReservaCreateDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        Reserva reserva = ventasService.crearReservaTemporal(dto, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    // 2. Consultar reservas del cliente - GET /api/reservas
    @Operation(summary = "Listar mis reservas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas del cliente")
    })
    @GetMapping("/reservas")
    public ResponseEntity<List<Reserva>> misReservas(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(ventasService.listarReservasCliente(clienteId));
    }

    // 3. Confirmar compra y emitir QR - POST /api/compras/confirmar
    @Operation(summary = "Confirmar compra",
            description = "Cobra con la pasarela simulada y emite las entradas con QR. Si se reintenta con una reserva ya pagada, devuelve la misma compra sin cobrar de nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra registrada"),
            @ApiResponse(responseCode = "402", description = "Pago rechazado: la reserva se cancela y los lugares se liberan"),
            @ApiResponse(responseCode = "403", description = "La reserva es de otro cliente"),
            @ApiResponse(responseCode = "404", description = "La reserva no existe"),
            @ApiResponse(responseCode = "409", description = "La reserva venció o fue cancelada")
    })
    @PostMapping("/compras/confirmar")
    public ResponseEntity<Compra> confirmarCompra(
            @RequestBody ConfirmarCompraDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        Compra compra = ventasService.confirmarCompra(dto, clienteId);
        return ResponseEntity.ok(compra);
    }

    // 4. Consultar compras y entradas con QR del cliente - GET /api/compras
    @Operation(summary = "Listar mis compras y entradas con QR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compras del cliente")
    })
    @GetMapping("/compras")
    public ResponseEntity<List<Compra>> misCompras(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(ventasService.listarComprasCliente(clienteId));
    }

    // 5. Solicitar devolución de una entrada vendida con al menos 72 horas de anticipación - POST /api/devoluciones/entradas/{entradaId}
    @Operation(summary = "Solicitar devolución de una entrada",
            description = "Hasta 72 horas antes del evento.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada"),
            @ApiResponse(responseCode = "403", description = "La entrada es de otro cliente"),
            @ApiResponse(responseCode = "404", description = "La entrada no existe"),
            @ApiResponse(responseCode = "409", description = "La entrada no está vendida, ya tiene una solicitud o faltan menos de 72 horas")
    })
    @PostMapping("/devoluciones/entradas/{entradaId}")
    public ResponseEntity<Devolucion> solicitarDevolucion(
            @PathVariable Long entradaId,
            @RequestBody SolicitudDevolucionDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        
        Devolucion devolucion = ventasService.solicitarDevolucion(entradaId, clienteId, dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucion);

    }

    // 6. Un ADMIN resuelve solicitud de devolución (aprobar o rechazar) - PUT /api/devoluciones/{devolucionId}/resolver
    @Operation(summary = "Aprobar o rechazar devolución",
            description = "Solo ADMIN. Si se aprueba, se libera el lugar y se pide el reintegro a la pasarela.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud resuelta"),
            @ApiResponse(responseCode = "400", description = "Falta el campo aprobar"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe"),
            @ApiResponse(responseCode = "409", description = "La solicitud ya fue resuelta")
    })
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