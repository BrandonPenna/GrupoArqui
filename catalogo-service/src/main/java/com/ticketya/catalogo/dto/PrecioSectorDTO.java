package com.ticketya.catalogo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrecioSectorDTO {
    private Long sectorId;
    private BigDecimal precio;
}