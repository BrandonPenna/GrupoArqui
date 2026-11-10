package com.ticketya.ventas_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventoCatalogoDTO {
    private LocalDateTime fechaHora;
    private String estado;
    private List<PrecioSectorCatalogoDTO> preciosSectores = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrecioSectorCatalogoDTO {
        private BigDecimal precio;
        private SectorCatalogoDTO sector;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SectorCatalogoDTO {
        private Long id;
        private Integer capacidad;
        private Boolean esNumerado;
        private Integer filas;
        private Integer asientosPorFila;
    }
}