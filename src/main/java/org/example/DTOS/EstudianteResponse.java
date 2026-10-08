package org.example.DTOS;

/*Responder con los datos de un estudiante*/
public record EstudianteResponse(
        Integer nroLegajo,
        String nombres,
        String apellido,
        Integer edad,
        String genero,
        Integer dni,
        String ciudadResidencia
) {}
