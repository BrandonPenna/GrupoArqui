package com.ticketya.ventas_service.controller;

import com.ticketya.ventas_service.dto.CancelacionEventoResultadoDTO;
import com.ticketya.ventas_service.service.VentasServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternoController {

    private final VentasServices ventasService;

    // Cancelar evento (lo llama catalogo-service) - POST /internal/eventos/{eventoId}/cancelacion
    @PostMapping("/eventos/{eventoId}/cancelacion")
    public ResponseEntity<CancelacionEventoResultadoDTO> cancelarEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(ventasService.cancelarEvento(eventoId));
    }
}
