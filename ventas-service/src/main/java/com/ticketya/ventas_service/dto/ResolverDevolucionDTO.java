package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResolverDevolucionDTO {
    @Schema(description = "true aprueba y reintegra, false rechaza", example = "true")
    private Boolean aprobar;
}