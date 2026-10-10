package org.example.DTOS;

/*Crear carrera*/
public record CreateCarreraRequest(
        Integer idCarrera,
        String nombre,
        Integer duracion
) {}
