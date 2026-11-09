package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AsientoPedidoDTO {
    // Opcionales si el sector es no numerado (ej. Campo)
    @Schema(description = "Fila (solo numerados)", example = "1")
    private Integer fila;
    @Schema(description = "Asiento (solo numerados)", example = "1")
    private Integer asiento;
    @Schema(description = "Precio de la entrada", example = "15000")
    private BigDecimal precio;
}