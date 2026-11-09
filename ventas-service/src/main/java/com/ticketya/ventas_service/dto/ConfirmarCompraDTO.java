package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConfirmarCompraDTO {
    @Schema(description = "ID de la reserva a pagar", example = "1")
    private Long reservaId;
    @Schema(description = "Medio de pago. RECHAZAR simula un pago rechazado", example = "TARJETA_CREDITO")
    private String medioPago; // Ej: "TARJETA_CREDITO", "MERCADO_PAGO", o "RECHAZAR" para probar fallo
}