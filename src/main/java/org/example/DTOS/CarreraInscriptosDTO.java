package org.example.DTOS;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CarreraInscriptosDTO {
    private Integer idCarrera;
    private String nombre;
    private Long cantidadInscriptos;

    @Override
    public String toString() {
        return "ID: " + idCarrera + " | Carrera: " + nombre + " | Inscriptos: " + cantidadInscriptos;
    }
}