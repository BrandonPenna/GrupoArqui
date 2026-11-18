package com.ticketya.ventas_service.controller;

import com.ticketya.ventas_service.dto.ValidarQrRequestDTO;
import com.ticketya.ventas_service.dto.ValidarQrResponseDTO;
import com.ticketya.ventas_service.service.VentasServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Control de Acceso", description = "Validación de entradas con código QR en el ingreso al evento (Req. 8)")
@RestController
@RequestMapping("/api/accesos")
@RequiredArgsConstructor
public class AccesoController {

    private final VentasServices ventasService;

    @Operation(summary = "Validar código QR en el ingreso",
            description = "Exclusivo para PERSONAL_ACCESO o ADMIN. Valida que el QR exista, pertenezca al evento y no haya sido usado, y lo marca como USADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acceso permitido y entrada marcada como USADA"),
            @ApiResponse(responseCode = "400", description = "Datos faltantes en la solicitud"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol PERSONAL_ACCESO ni ADMIN"),
            @ApiResponse(responseCode = "404", description = "El código QR no existe"),
            @ApiResponse(responseCode = "409", description = "El QR ya fue utilizado, pertenece a otro evento o la entrada fue devuelta/cancelada")
    })
    @PostMapping("/validar-qr")
    public ResponseEntity<ValidarQrResponseDTO> validarQr(
            @RequestBody ValidarQrRequestDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "acceso-1") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "PERSONAL_ACCESO") String role) {

        if (!"PERSONAL_ACCESO".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(ventasService.validarAccesoQR(dto, userId));
    }
}