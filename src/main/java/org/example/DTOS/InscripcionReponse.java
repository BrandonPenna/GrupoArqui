package org.example.DTOS;

/*Datos de cada inscripcion*/
public record InscripcionReponse(
        Integer idCarrera,
        String nombreCarrera,
        Integer anioInscripcion,
        Integer anioGraduacion,
        boolean graduado
) {
}
