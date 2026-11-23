package com.ticketya.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private String destinatarioId;
    private String canal; // EMAIL o PUSH
    private String asunto;
    private String mensaje;
    private LocalDateTime fechaEnvio;
}
