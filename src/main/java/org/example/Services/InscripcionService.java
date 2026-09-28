package org.example.Services;

import org.example.Entity.InscripcionId;
import org.example.Entity.Inscripcion;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;
import org.example.Repository.CarreraRepository;
import org.example.Repository.EstudianteRepository;
import org.example.Repository.InscripcionRepository;

public class InscripcionService {
    private static  InscripcionService instance;
    private final InscripcionRepository repoInscripciones;

    private InscripcionService() {
        this.repoInscripciones = InscripcionRepository.getInstance();
    }

    public static InscripcionService getInstance(){
        if(instance==null){
            instance= new InscripcionService();
        }
        return instance;
    }


    public Inscripcion matricular(Integer nroLegajo, Integer idCarrera, Integer anioInscripcion) {
        if (!EstudianteRepository.getInstance().existePorLegajo(nroLegajo)) {
            throw new IllegalArgumentException("No existe un estudiante con ese legajo");
        }
        if (!CarreraRepository.getInstance().existePorId(idCarrera)) {
            throw new IllegalArgumentException("No existe una carrera con ese id");
        }

        Inscripcion inscripcion = new Inscripcion(nroLegajo, idCarrera, false);
        inscripcion.setAnioInscripcion(anioInscripcion);

        return this.repoInscripciones.persist(inscripcion);
    }

    public void eliminarInscripcion(Integer nroLegajo, Integer idCarrera) {
        InscripcionId id = new InscripcionId(nroLegajo, idCarrera);
        this.repoInscripciones.delete(id);
    }
}
