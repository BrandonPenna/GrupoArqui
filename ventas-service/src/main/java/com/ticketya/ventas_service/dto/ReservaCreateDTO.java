package com.ticketya.ventas_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class ReservaCreateDTO {
    @Schema(description = "ID del evento", example = "1")
    private Long eventoId;
    @Schema(description = "ID del sector", example = "1")
    private Long sectorId;
    @Schema(description = "Lugares pedidos")
    private List<AsientoPedidoDTO> asientos;
}
