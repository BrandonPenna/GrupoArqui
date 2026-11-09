package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ticketya.catalogo.model.TipoEvento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventoCreateDTO {
    @Schema(description = "Nombre del evento", example = "Recital de Prueba")
    private String nombre;
    @Schema(description = "Descripción", example = "Evento para probar la API")
    private String descripcion;
    @Schema(description = "Tipo de evento", example = "RECITAL")
    private TipoEvento tipo;
    @Schema(description = "Artistas, equipos o disertantes", example = "[\"Banda A\"]")
    private List<String> participantes;
    @Schema(description = "Fecha y hora del evento", example = "2026-12-15T20:00:00")
    private LocalDateTime fechaHora;
    @Schema(description = "ID del lugar", example = "1")
    private Long lugarId;
    @Schema(description = "Precio de cada sector del lugar")
    private List<PrecioSectorDTO> precios;
}