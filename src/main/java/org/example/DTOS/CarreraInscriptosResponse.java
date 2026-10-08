package org.example.DTOS;


/*Carreras ordenadas por cantidad de inscriptos*/
public record CarreraInscriptosResponse(
        Integer idCarrera,
        String nombre,
        Long cantidadInscriptos
) { }