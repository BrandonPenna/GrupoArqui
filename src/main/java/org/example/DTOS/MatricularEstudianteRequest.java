package org.example.DTOS;

/*Matricular estudiante en una carrera*/
public record MatricularEstudianteRequest(
        Integer nroLegajo,
        Integer idCarrera,
        Integer anioInscripcion
) {}
