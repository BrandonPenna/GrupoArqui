package org.example.DTOS;

/*Reporte: inscriptos y egresados de una carrera en un anio*/
public record ReporteCarreraResponse(
        String nombreCarrera,
        Integer anio,
        Long inscriptos,
        Long egresados
) {}