package org.example.Repository;

import java.util.List;

import org.example.DTOS.CarreraInscriptosResponse;
import org.example.Entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    // 2f) Carreras con estudiantes inscriptos, ordenadas por cantidad de inscriptos
    @Query("SELECT new org.example.DTOS.CarreraInscriptosResponse(c.idCarrera, c.nombre, COUNT(i)) " +
            "FROM Carrera c JOIN c.inscripciones i " +
            "GROUP BY c.idCarrera, c.nombre " +
            "ORDER BY COUNT(i) DESC")
    List<CarreraInscriptosResponse> obtenerCarrerasConInscriptosOrdenadas();


    // 2h) Reporte: inscriptos y egresados por carrera y anio (SQL nativo)
    @Query(value = "SELECT c.nombre, t.anio, SUM(t.inscripto), SUM(t.egresado) " +
            "FROM carrera c " +
            "JOIN ( " +
            "    SELECT id_carrera, anio_inscripcion AS anio, 1 AS inscripto, 0 AS egresado " +
            "    FROM inscripcion " +
            "    UNION ALL " +
            "    SELECT id_carrera, graduacion AS anio, 0 AS inscripto, 1 AS egresado " +
            "    FROM inscripcion " +
            "    WHERE graduacion > 0 " +
            ") t ON t.id_carrera = c.id_carrera " +
            "GROUP BY c.id_carrera, c.nombre, t.anio " +
            "ORDER BY c.nombre ASC, t.anio ASC",
            nativeQuery = true)
    List<java.lang.Object[]> obtenerReporteCarreras();
}