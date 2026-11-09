package com.ticketya.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ticketya.catalogo.dto.LugarDTO;
import com.ticketya.catalogo.model.Lugar;
import com.ticketya.catalogo.service.LugarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lugares", description = "Lugares y sus sectores")
@RestController
@RequestMapping("/api/lugares")
@RequiredArgsConstructor
public class LugarController {

    private final LugarService lugarService;

    @Operation(summary = "Crear lugar con sus sectores",
            description = "ADMIN u ORGANIZADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar creado"),
            @ApiResponse(responseCode = "403", description = "Rol sin permiso")
    })
    @PostMapping
    public ResponseEntity<Lugar> crearLugar(
            @RequestBody LugarDTO dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "ORGANIZADOR") String role) {
        
        if (!"ORGANIZADOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lugarService.crearLugar(dto));
    }

    @Operation(summary = "Listar lugares")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de lugares con sus sectores")
    })
    @GetMapping
    public ResponseEntity<List<Lugar>> listarLugares() {
        return ResponseEntity.ok(lugarService.listarTodos());
    }

    @Operation(summary = "Ver un lugar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "El lugar no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Lugar> obtenerLugar(@PathVariable Long id) {
        return ResponseEntity.ok(lugarService.buscarPorId(id));
    }
}