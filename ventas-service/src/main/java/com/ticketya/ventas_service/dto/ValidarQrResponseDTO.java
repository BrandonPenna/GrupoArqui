package com.ticketya.ventas_service.dto;

import com.ticketya.ventas_service.model.EstadoEntrada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidarQrResponseDTO {
    private boolean accesoPermitido;
    private String mensaje;
    private Long entradaId;
    private Long eventoId;
    private Long sectorId;
    private Integer fila;
    private Integer asiento;
    private EstadoEntrada estado;
    private LocalDateTime fechaIngreso;
}