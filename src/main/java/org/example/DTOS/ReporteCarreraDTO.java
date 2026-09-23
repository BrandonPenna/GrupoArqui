package org.example.DTOS;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReporteCarreraDTO {
    private String nombreCarrera;
    private Integer anio;
    private Long inscriptos;
    private Long egresados;

    @Override
    public String toString() {
        return "Carrera: " + nombreCarrera + " | Año: " + anio + 
               " | Inscriptos: " + inscriptos + " | Egresados: " + egresados;
    }
}