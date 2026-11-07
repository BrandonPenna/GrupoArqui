package com.ticketya.ventas_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AsientoPedidoDTO {
    // Opcionales si el sector es no numerado (ej. Campo)
    private Integer fila;
    private Integer asiento;
    private BigDecimal precio;
}