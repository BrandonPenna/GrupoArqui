package com.ticketya.ventas_service.controller;

import com.ticketya.ventas_service.dto.EstadoFilaVirtualDTO;
import com.ticketya.ventas_service.service.FilaVirtualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Fila Virtual", description = "Gestión de fila virtual por orden de llegada y tandas para eventos de alta demanda (Req. 6)")
@RestController
@RequestMapping("/api/fila-virtual")
@RequiredArgsConstructor
public class FilaVirtualController {

    private final FilaVirtualService filaVirtualService;

    @Operation(summary = "Ingresar a la fila virtual de un evento de alta demanda",
            description = "Registra al cliente en orden de llegada, informa su posición y si ya está habilitado en la tanda actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posición en la fila virtual")
    })
    @PostMapping("/eventos/{eventoId}/ingresar")
    public ResponseEntity<EstadoFilaVirtualDTO> ingresar(
            @PathVariable Long eventoId,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(filaVirtualService.ingresarAFila(eventoId, clienteId));
    }

    @Operation(summary = "Consultar mi posición y estado en la fila virtual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actual en la fila"),
            @ApiResponse(responseCode = "404", description = "El cliente no ingresó a la fila de este evento")
    })
    @GetMapping("/eventos/{eventoId}/estado")
    public ResponseEntity<EstadoFilaVirtualDTO> consultarEstado(
            @PathVariable Long eventoId,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(filaVirtualService.consultarEstado(eventoId, clienteId));
    }

    @Operation(summary = "Avanzar a la siguiente tanda de la fila virtual (Admin)")
    @PostMapping("/eventos/{eventoId}/avanzar-tanda")
    public ResponseEntity<Map<String, Object>> avanzarTanda(@PathVariable Long eventoId) {
        int restantes = filaVirtualService.avanzarTanda(eventoId);
        return ResponseEntity.ok(Map.of(
                "eventoId", eventoId,
                "clientesRestantesEnFila", restantes,
                "mensaje", "Se habilitó la siguiente tanda de la fila virtual"
        ));
    }
}
