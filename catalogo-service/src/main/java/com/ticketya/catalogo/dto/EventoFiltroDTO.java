package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ticketya.catalogo.model.TipoEvento;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class EventoFiltroDTO {
    @Schema(description = "Texto libre en nombre, descripción o participantes", example = "piojos")
    private String q;
    @Schema(description = "Tipo de evento", example = "RECITAL")
    private TipoEvento tipo;
    @Schema(description = "ID del organizador", example = "org-1")
    private String organizadorId;
    @Schema(description = "Nombre de un participante", example = "Banda A")
    private String participante;
    @Schema(description = "Ciudad del lugar", example = "Tandil")
    private String ciudad;

    @Schema(description = "Desde esta fecha y hora", example = "2026-12-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaDesde;

    @Schema(description = "Hasta esta fecha y hora", example = "2026-12-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHasta;
}