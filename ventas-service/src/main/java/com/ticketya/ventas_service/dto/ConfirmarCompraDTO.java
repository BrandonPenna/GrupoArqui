package com.ticketya.ventas_service.dto;

import lombok.Data;

@Data
public class ConfirmarCompraDTO {
    private Long reservaId;
    private String medioPago; // Ej: "TARJETA_CREDITO", "MERCADO_PAGO", o "RECHAZAR" para probar fallo
}