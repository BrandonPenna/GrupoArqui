package com.ticketya.ventas_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFilaVirtualDTO {
    private Long eventoId;
    private String clienteId;
    private int posicion;
    private int totalEnFila;
    private int tamanioTanda;
    private boolean habilitadoParaComprar;
    private String mensaje;
}
