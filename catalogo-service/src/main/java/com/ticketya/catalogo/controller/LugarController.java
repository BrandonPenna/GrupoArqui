package com.ticketya.catalogo.controller;

import com.ticketya.catalogo.dto.LugarDTO;
import com.ticketya.catalogo.model.Lugar;
import com.ticketya.catalogo.service.LugarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lugares")
@RequiredArgsConstructor
public class LugarController {

    private final LugarService lugarService;

    @PostMapping
    public ResponseEntity<Lugar> crearLugar(
            @RequestBody LugarDTO dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "ORGANIZADOR") String role) {
        
        if (!"ORGANIZADOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lugarService.crearLugar(dto));
    }

    @GetMapping
    public ResponseEntity<List<Lugar>> listarLugares() {
        return ResponseEntity.ok(lugarService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lugar> obtenerLugar(@PathVariable Long id) {
        return ResponseEntity.ok(lugarService.buscarPorId(id));
    }
}