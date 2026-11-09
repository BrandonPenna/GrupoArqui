package com.ticketya.ventas_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ticketya.ventas_service.dto.CancelacionEventoResultadoDTO;
import com.ticketya.ventas_service.service.VentasServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Interno", description = "Endpoints entre servicios. No se exponen por el gateway")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternoController {

    private final VentasServices ventasService;

    // Cancelar evento (lo llama catalogo-service) - POST /internal/eventos/{eventoId}/cancelacion
    @Operation(summary = "Procesar cancelación de un evento",
            description = "Lo llama catalogo-service. Cancela reservas pendientes, reintegra entradas vendidas y notifica. Es idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado de la cancelación")
    })
    @PostMapping("/eventos/{eventoId}/cancelacion")
    public ResponseEntity<CancelacionEventoResultadoDTO> cancelarEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(ventasService.cancelarEvento(eventoId));
    }
}
