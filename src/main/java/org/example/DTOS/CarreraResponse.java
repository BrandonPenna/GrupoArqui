package org.example.DTOS;

/*Responder con los datos de una carrera*/
public record CarreraResponse(
        Integer idCarrera,
        String nombre,
        Integer duracion
) {}
