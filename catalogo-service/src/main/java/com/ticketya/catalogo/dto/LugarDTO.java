package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class LugarDTO {
    @Schema(description = "Nombre del lugar", example = "Teatro de Prueba")
    private String nombre;
    @Schema(description = "Dirección", example = "Calle 123")
    private String direccion;
    @Schema(description = "Ciudad", example = "Tandil")
    private String ciudad;
    @Schema(description = "Latitud", example = "-37.32")
    private Double latitud;
    @Schema(description = "Longitud", example = "-59.13")
    private Double longitud;
    @Schema(description = "Sectores del lugar")
    private List<SectorDTO> sectores;
}