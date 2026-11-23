package com.ticketya.ventas_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EventoCatalogoDTO {
    private Long id;
    private String nombre;
    private String organizadorId;
    private LocalDateTime fechaHora;
    private String estado;
    private List<PrecioSectorCatalogoDTO> preciosSectores = new ArrayList<>();

    @Data
    public static class PrecioSectorCatalogoDTO {
        private Long id;
        private BigDecimal precio;
        private SectorCatalogoDTO sector;
    }

    @Data
    public static class SectorCatalogoDTO {
        private Long id;
        private String nombre;
        private Integer capacidad;
        private Boolean esNumerado;
        private Integer filas;
        private Integer asientosPorFila;
    }
}
