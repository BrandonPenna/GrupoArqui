package com.ticketya.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import com.ticketya.catalogo.dto.EventoCreateDTO;
import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Eventos", description = "Creación, publicación, suspensión, cancelación y búsqueda de eventos")
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // 1. Crear evento en borrador (Solo Organizador)
    @Operation(summary = "Crear evento en borrador",
            description = "Solo ORGANIZADOR. El evento queda en estado BORRADOR con los precios de cada sector.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "403", description = "El rol no es ORGANIZADOR"),
            @ApiResponse(responseCode = "404", description = "El lugar o un sector no existe")
    })
    @PostMapping
    public ResponseEntity<Evento> crearEvento(
            @RequestBody EventoCreateDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "org-1") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ORGANIZADOR") String role) {

        if (!"ORGANIZADOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crearEventoBorrador(dto, userId));
    }

    // 2. Publicar evento (Solo Organizador dueño del evento)
    @Operation(summary = "Publicar evento",
            description = "Solo el ORGANIZADOR que creó el evento. Pasa de BORRADOR a PUBLICADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento publicado"),
            @ApiResponse(responseCode = "403", description = "El rol no es ORGANIZADOR o el evento es de otro organizador"),
            @ApiResponse(responseCode = "404", description = "El evento no existe"),
            @ApiResponse(responseCode = "409", description = "El evento no tiene precios por sector")
    })
    @PutMapping("/{id}/publicar")
    public ResponseEntity<Evento> publicarEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "org-1") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ORGANIZADOR") String role) {

        if (!"ORGANIZADOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventoService.publicarEvento(id, userId));
    }

    // 3. Cancelar evento (Admin u Organizador)
    @Operation(summary = "Cancelar evento",
            description = "ADMIN, u ORGANIZADOR dueño del evento. Si tenía ventas, ventas-service reintegra todas las entradas y notifica a los compradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento cancelado"),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso o evento de otro organizador"),
            @ApiResponse(responseCode = "404", description = "El evento no existe"),
            @ApiResponse(responseCode = "409", description = "El evento ya está cancelado o finalizado"),
            @ApiResponse(responseCode = "503", description = "ventas-service no respondió: el evento no se cancela")
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Evento> cancelarEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "admin-1") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String role) {

        if (!"ADMIN".equalsIgnoreCase(role) && !"ORGANIZADOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventoService.cancelarEvento(id, userId, role));
    }

    // Suspender evento (Solo Admin)
    @Operation(summary = "Suspender evento",
            description = "Solo ADMIN. Mientras está suspendido no aparece en la búsqueda ni acepta reservas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento suspendido"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "El evento no existe"),
            @ApiResponse(responseCode = "409", description = "El evento no está publicado ni agotado")
    })
    @PutMapping("/{id}/suspender")
    public ResponseEntity<Evento> suspenderEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventoService.suspenderEvento(id));
    }

    // Reactivar evento suspendido (Solo Admin)
    @Operation(summary = "Reactivar evento suspendido",
            description = "Solo ADMIN. Vuelve a PUBLICADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento reactivado"),
            @ApiResponse(responseCode = "403", description = "El rol no es ADMIN"),
            @ApiResponse(responseCode = "404", description = "El evento no existe"),
            @ApiResponse(responseCode = "409", description = "El evento no está suspendido")
    })
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Evento> reactivarEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventoService.reactivarEvento(id));
    }

    // 4. Búsqueda multicriterio con ordenamiento y paginación (Punto 9)
    @Operation(summary = "Buscar eventos publicados",
            description = "Filtros combinables y paginados. Orden: sort=fechaHora,asc (por defecto), sort=precioDesde,asc (precio) u orden=relevancia junto con q.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de eventos")
    })
    @GetMapping
    public ResponseEntity<Page<Evento>> buscarEventos(
            @ParameterObject @ModelAttribute EventoFiltroDTO filtro,
            @RequestParam(required = false) String orden,
            @ParameterObject @PageableDefault(size = 10, sort = "fechaHora") Pageable pageable) {
        return ResponseEntity.ok(eventoService.buscarEventos(filtro, orden, pageable));
    }

    // 5. Ver detalle de un evento por ID
    @Operation(summary = "Ver detalle de un evento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "El evento no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Evento> obtenerEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }
}