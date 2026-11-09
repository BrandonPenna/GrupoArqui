package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrecioSectorDTO {
    @Schema(description = "ID del sector", example = "1")
    private Long sectorId;
    @Schema(description = "Precio de la entrada en ese sector", example = "15000")
    private BigDecimal precio;
}