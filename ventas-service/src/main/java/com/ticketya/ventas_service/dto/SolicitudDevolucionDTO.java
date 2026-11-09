package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SolicitudDevolucionDTO {
    @Schema(description = "Motivo de la devolución", example = "No puedo asistir")
    private String motivo;
}
