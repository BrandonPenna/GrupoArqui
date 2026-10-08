package org.example.DTOS;


import java.util.List;

/*Responder con un estudiante y sus carreras*/
public record EstudianteDetalleReponse<InscripcionResponse>(
        Integer nroLegajo,
        String nombres,
        String apellido,
        Integer edad,
        String genero,
        Integer dni,
        String ciudadResidencia,
        List<InscripcionResponse> inscripciones
) {
}
