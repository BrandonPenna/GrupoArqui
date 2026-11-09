package com.ticketya.ventas_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelacionEventoResultadoDTO {
    private Long eventoId;
    private int reservasCanceladas;
    private int entradasReintegradas;
    private int clientesNotificados;
}
