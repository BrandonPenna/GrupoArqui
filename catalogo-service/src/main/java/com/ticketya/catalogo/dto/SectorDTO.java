package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SectorDTO {
    @Schema(description = "Nombre del sector", example = "Platea")
    private String nombre;
    @Schema(description = "Capacidad total del sector", example = "100")
    private Integer capacidad;
    @Schema(description = "true si tiene filas y asientos, false si es una zona como Campo", example = "true")
    private Boolean esNumerado;
    @Schema(description = "Cantidad de filas (solo numerados)", example = "10")
    private Integer filas;
    @Schema(description = "Asientos por fila (solo numerados)", example = "10")
    private Integer asientosPorFila;
}