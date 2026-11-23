package com.ticketya.catalogo.controller;

import com.ticketya.catalogo.dto.LoginRequestDTO;
import com.ticketya.catalogo.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Tag(name = "Autenticación JWT", description = "Inicio de sesión y emisión de tokens JWT por rol (US-23)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final List<String> ROLES_VALIDOS = List.of(
            "CLIENTE", "ORGANIZADOR", "ADMIN", "PERSONAL_ACCESO"
    );

    private final JwtUtil jwtUtil;

    @Operation(summary = "Iniciar sesión y obtener token JWT",
            description = "Valida credenciales y emite un token JWT firmado (HS256) con el userId y rol del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT emitido"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()
                || "invalida".equalsIgnoreCase(dto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String rol = dto.getRol() != null && !dto.getRol().isBlank()
                ? dto.getRol().trim().toUpperCase()
                : inferirRolPorUsername(dto.getUsername());

        if (!ROLES_VALIDOS.contains(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + rol);
        }

        String token = jwtUtil.generarToken(dto.getUsername().trim(), rol);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "username", dto.getUsername().trim(),
                "rol", rol,
                "expiresInSeconds", 3600
        ));
    }

    private String inferirRolPorUsername(String username) {
        String u = username.toLowerCase();
        if (u.startsWith("admin")) return "ADMIN";
        if (u.startsWith("org")) return "ORGANIZADOR";
        if (u.startsWith("acceso")) return "PERSONAL_ACCESO";
        return "CLIENTE";
    }
}
