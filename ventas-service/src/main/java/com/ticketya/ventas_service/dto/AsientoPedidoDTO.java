package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AsientoPedidoDTO {
    // Opcionales si el sector es no numerado (ej. Campo)
    @Schema(description = "Fila (solo numerados)", example = "1")
    private Integer fila;
    @Schema(description = "Asiento (solo numerados)", example = "1")
    private Integer asiento;
}
