package com.ticketya.catalogo.dto;

import lombok.Data;
import java.util.List;

@Data
public class LugarDTO {
    private String nombre;
    private String direccion;
    private String ciudad;
    private Double latitud;
    private Double longitud;
    private List<SectorDTO> sectores;
}