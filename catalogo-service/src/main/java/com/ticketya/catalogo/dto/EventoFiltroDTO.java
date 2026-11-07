package com.ticketya.catalogo.dto;

import com.ticketya.catalogo.model.TipoEvento;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class EventoFiltroDTO {
    private TipoEvento tipo;
    private String organizadorId;
    private String participante;
    private String ciudad;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHasta;
}