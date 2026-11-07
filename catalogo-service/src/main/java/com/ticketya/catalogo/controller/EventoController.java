package com.ticketya.catalogo.controller;

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

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // 1. Crear evento en borrador (Solo Organizador)
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
    @PutMapping("/{id}/publicar")
    public ResponseEntity<Evento> publicarEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "org-1") String userId) {
        return ResponseEntity.ok(eventoService.publicarEvento(id, userId));
    }

    // 3. Cancelar evento (Admin u Organizador)
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Evento> cancelarEvento(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String role) {

        if (!"ADMIN".equalsIgnoreCase(role) && !"ORGANIZADOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(eventoService.cancelarEvento(id));
    }

    // 4. Búsqueda multicriterio con ordenamiento y paginación (Punto 9)
    // Ej: /api/eventos?ciudad=Tandil&tipo=RECITAL&sort=fechaHora,asc
    @GetMapping
    public ResponseEntity<Page<Evento>> buscarEventos(
            @ModelAttribute EventoFiltroDTO filtro,
            @PageableDefault(size = 10, sort = "fechaHora") Pageable pageable) {
        return ResponseEntity.ok(eventoService.buscarEventos(filtro, pageable));
    }

    // 5. Ver detalle de un evento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Evento> obtenerEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }
}