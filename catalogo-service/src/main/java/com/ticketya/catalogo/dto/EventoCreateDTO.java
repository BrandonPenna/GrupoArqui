package com.ticketya.catalogo.dto;

import com.ticketya.catalogo.model.TipoEvento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventoCreateDTO {
    private String nombre;
    private String descripcion;
    private TipoEvento tipo;
    private List<String> participantes;
    private LocalDateTime fechaHora;
    private Long lugarId;
    private List<PrecioSectorDTO> precios;
}