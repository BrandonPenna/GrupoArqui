package org.example.Repository;


import org.example.DTOS.CarreraInscriptosDTO;
import org.example.DTOS.ReporteCarreraDTO;
import org.example.Entity.Carrera;

import java.util.ArrayList;
import java.util.List;

public class CarreraRepository extends BaseJPARepository<Carrera, Integer> {

    private static CarreraRepository instance;

    private CarreraRepository() {
        super(Carrera.class, Integer.class);
    }

    public static CarreraRepository getInstance() {
        if (instance == null) {
            instance = new CarreraRepository();
        }
        return instance;
    }

    public boolean existePorId(Integer idCarrera) {
        Long cantidad = entityManager
            .createQuery(
                    "SELECT COUNT(e) FROM Carrera e WHERE e.idCarrera = :idCarrera",
                    Long.class
            )
            .setParameter("idCarrera", idCarrera)
            .getSingleResult();

        return cantidad > 0;
    }

    public List<CarreraInscriptosDTO> obtenerCarrerasConInscriptosOrdenadas() {
        String jpql = "SELECT new org.example.DTOS.CarreraInscriptosDTO(c.idCarrera, c.nombre, COUNT(i)) " +
                "FROM Carrera c JOIN c.inscripciones i " +
                "GROUP BY c.idCarrera, c.nombre " +
                "ORDER BY COUNT(i) DESC";
        try {
            return entityManager.createQuery(jpql, CarreraInscriptosDTO.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Punto 3: Reporte de inscriptos y egresados por carrera y año (SQL nativo).
    // Cada inscripción aporta una fila en su año de inscripción (inscripto = 1)
    // y, si se graduó, otra fila en su año de graduación (egresado = 1).
    // Luego se agrupa por carrera y año, ordenando alfabética y cronológicamente.
    public List<ReporteCarreraDTO> obtenerReporteCarreras() {
        String sql = "SELECT c.nombre, t.anio, SUM(t.inscripto), SUM(t.egresado) " +
                     "FROM carrera c " +
                     "JOIN ( " +
                     "    SELECT id_carrera, anioInscripcion AS anio, 1 AS inscripto, 0 AS egresado " +
                     "    FROM Inscripcion " +
                     "    UNION ALL " +
                     "    SELECT id_carrera, graduacion AS anio, 0 AS inscripto, 1 AS egresado " +
                     "    FROM Inscripcion " +
                     "    WHERE graduacion > 0 " +
                     ") t ON t.id_carrera = c.id_carrera " +
                     "GROUP BY c.id_carrera, c.nombre, t.anio " +
                     "ORDER BY c.nombre ASC, t.anio ASC";
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> filas = entityManager.createNativeQuery(sql).getResultList();

            List<ReporteCarreraDTO> reporte = new ArrayList<>();
            for (Object[] fila : filas) {
                reporte.add(new ReporteCarreraDTO(
                        (String) fila[0],
                        ((Number) fila[1]).intValue(),
                        ((Number) fila[2]).longValue(),
                        ((Number) fila[3]).longValue()
                ));
            }
            return reporte;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    
}