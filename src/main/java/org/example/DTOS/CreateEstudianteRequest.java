package org.example.DTOS;

import java.time.LocalDate;
/*Crear estudiante*/
public record CreateEstudianteRequest(
        String nombres,
        String apellido,
        LocalDate fechaNacimiento,
        String genero,
        Integer dni,
        String ciudadResidencia,
        Integer nroLegajo
) {}
