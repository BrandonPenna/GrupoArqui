package org.example.Repository;

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
}
