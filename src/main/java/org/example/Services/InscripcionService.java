package org.example.Services;

import org.example.DTOS.InscripcionId;
import org.example.Entity.Inscripcion;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

public class InscripcionService {

    private final RepositoryFactory factory;

    public InscripcionService() {
        this.factory = JPARepositoryFactory.getInstance();
    }

    public Inscripcion matricular(Integer nroLegajo, Integer idCarrera, Integer anioInscripcion) {
        if (!factory.getEstudianteRepository().existePorLegajo(nroLegajo)) {
            throw new IllegalArgumentException("No existe un estudiante con ese legajo");
        }
        if (!factory.getCarreraRepository().existePorId(idCarrera)) {
            throw new IllegalArgumentException("No existe una carrera con ese id");
        }

        Inscripcion inscripcion = new Inscripcion(nroLegajo, idCarrera, false);
        inscripcion.setAnioInscripcion(anioInscripcion);

        return factory.getInscripcionRepository().persist(inscripcion);
    }

    public void eliminarInscripcion(Integer nroLegajo, Integer idCarrera) {
        InscripcionId id = new InscripcionId(nroLegajo, idCarrera);
        factory.getInscripcionRepository().delete(id);
    }
}
