package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ValidarQrRequestDTO {

    @Schema(description = "ID del evento en el que se está controlando el acceso", example = "1")
    private Long eventoId;

    @Schema(description = "Token único del código QR escaneado", example = "QR-b8b2ecf3-8f74-4c1b-be6b-363be6f4ce6c")
    private String codigoQR;
}