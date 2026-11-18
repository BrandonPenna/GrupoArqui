package com.ticketya.ventas_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSectorDTO {
    private Long sectorId;
    private String nombreSector;
    private Integer capacidad;
    private long entradasVendidas;
    private long entradasUtilizadas;
    private long entradasDevueltas;
    private double porcentajeOcupacion;
    private BigDecimal recaudacion;
}