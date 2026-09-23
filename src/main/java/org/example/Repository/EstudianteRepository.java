package org.example.Repository;

import java.util.ArrayList;
import java.util.List;

import org.example.Entity.Estudiante;

public class EstudianteRepository extends BaseJPARepository<Estudiante, Integer> {

    private static EstudianteRepository instance;

    private EstudianteRepository() {
        super(Estudiante.class, Integer.class);
    }

    public static EstudianteRepository getInstance() {
        if (instance == null) {
            instance = new EstudianteRepository();
        }
        return instance;
    }

    public boolean existePorDni(Integer dni) {
        Long cantidad = entityManager
            .createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.dni = :dni",
                    Long.class
            )
            .setParameter("dni", dni)
            .getSingleResult();

        return cantidad > 0;
    }

    public boolean existePorLegajo(Integer nroLegajo) {
        Long cantidad = entityManager
            .createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.nroLegajo = :nroLegajo",
                    Long.class
            )
            .setParameter("nroLegajo", nroLegajo)
            .getSingleResult();

        return cantidad > 0;
    }

    public List<Estudiante> obtenerEstudiantesOrderByLegajo(){
        String jpql = "SELECT e FROM Estudiante e ORDER BY e.nroLegajo ASC";

        try{
            List<Estudiante> estudiantes = entityManager.createQuery(jpql, Estudiante.class).getResultList();
            return estudiantes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
