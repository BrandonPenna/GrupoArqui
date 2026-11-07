package com.ticketya.ventas_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReservaCreateDTO {
    private Long eventoId;
    private Long sectorId;
    private Boolean esNumerado;
    private Integer capacidadMaximaSector;
    private List<AsientoPedidoDTO> asientos;
}