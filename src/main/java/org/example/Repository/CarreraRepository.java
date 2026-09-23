package org.example.Repository;


import org.example.DTOS.CarreraInscriptosDTO;
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

    // Punto 3: Inscriptos agrupados por carrera y año
    public List<Object[]> obtenerInscriptosPorAnio() {
        String jpql = "SELECT c.nombre, i.anioInscripcion, COUNT(i) " +
                      "FROM Inscripcion i JOIN i.carrera c " +
                      "GROUP BY c.nombre, i.anioInscripcion " +
                      "ORDER BY c.nombre ASC, i.anioInscripcion ASC";
        try {
            return entityManager.createQuery(jpql, Object[].class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Punto 3: Egresados agrupados por carrera y año
    public List<Object[]> obtenerEgresadosPorAnio() {
        String jpql = "SELECT c.nombre, i.anioGraduacion, COUNT(i) " +
                      "FROM Inscripcion i JOIN i.carrera c " +
                      "WHERE i.anioGraduacion > 0 " +
                      "GROUP BY c.nombre, i.anioGraduacion " +
                      "ORDER BY c.nombre ASC, i.anioGraduacion ASC";
        try {
            return entityManager.createQuery(jpql, Object[].class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    
}