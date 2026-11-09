package com.ticketya.ventas_service.dto;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EventoCatalogoDTO {
    private LocalDateTime fechaHora;
    private String estado;

}