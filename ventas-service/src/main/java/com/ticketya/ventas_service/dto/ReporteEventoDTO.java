package com.ticketya.ventas_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEventoDTO {
    private Long eventoId;
    private String nombreEvento;
    private String organizadorId;
    private String estadoEvento;
    private Integer capacidadTotal;
    private long totalEntradasVendidas;
    private long totalEntradasUtilizadas;
    private long totalEntradasDevueltas;
    private double porcentajeOcupacionTotal;
    private BigDecimal recaudacionTotal;
    private List<ReporteSectorDTO> sectores;
}