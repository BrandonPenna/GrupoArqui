package org.example.Repository;


import org.example.DTOS.InscripcionId;
import org.example.Entity.Inscripcion;

public class InscripcionRepository extends BaseJPARepository<Inscripcion, InscripcionId> {

    private static InscripcionRepository instance;

    private InscripcionRepository() {
        super(Inscripcion.class, InscripcionId.class);
    }

    public static InscripcionRepository getInstance() {
        if (instance == null) {
            instance = new InscripcionRepository();
        }
        return instance;
    }
}