package com.ticketya.catalogo.controller;

import com.ticketya.catalogo.dto.NotificacionDTO;
import com.ticketya.catalogo.dto.PreferenciaClienteDTO;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.PreferenciaClienteDocument;
import com.ticketya.catalogo.service.NotificacionService;
import com.ticketya.catalogo.service.PreferenciaRecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Preferencias y Recomendaciones (NoSQL)",
     description = "Gestión documental de preferencias de clientes, recomendaciones de eventos y avisos (Req. 10 y 11)")
@RestController
@RequestMapping("/api/preferencias")
@RequiredArgsConstructor
public class PreferenciaController {

    private final PreferenciaRecomendacionService preferenciaService;
    private final NotificacionService notificacionService;

    @Operation(summary = "Guardar o actualizar mis preferencias (NoSQL)",
            description = "Registra tipos de evento, artistas favoritos y ciudad del cliente en la colección documental.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias guardadas")
    })
    @PutMapping
    public ResponseEntity<PreferenciaClienteDocument> guardarPreferencias(
            @RequestBody PreferenciaClienteDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(preferenciaService.guardarPreferencias(clienteId, dto));
    }

    @Operation(summary = "Consultar mis preferencias guardadas")
    @GetMapping
    public ResponseEntity<PreferenciaClienteDocument> obtenerPreferencias(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(preferenciaService.obtenerPreferencias(clienteId));
    }

    @Operation(summary = "Obtener recomendaciones personalizadas de eventos (Req. 10 / US-20)",
            description = "Sugiere eventos publicados priorizando artistas favoritos, ciudad y tipos de evento del cliente.")
    @GetMapping("/recomendaciones")
    public ResponseEntity<List<Evento>> obtenerRecomendaciones(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(preferenciaService.recomendarEventos(clienteId));
    }

    @Operation(summary = "Consultar notificaciones recibidas por el cliente (Req. 11 / US-21)")
    @GetMapping("/notificaciones")
    public ResponseEntity<List<NotificacionDTO>> misNotificaciones(
            @RequestHeader(value = "X-User-Id", defaultValue = "cliente-1") String clienteId) {
        return ResponseEntity.ok(notificacionService.listarNotificacionesCliente(clienteId));
    }
}
