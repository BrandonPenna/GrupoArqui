package com.ticketya.catalogo.dto;

import lombok.Data;

@Data
public class SectorDTO {
    private String nombre;
    private Integer capacidad;
    private Boolean esNumerado;
    private Integer filas;
    private Integer asientosPorFila;
}