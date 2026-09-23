package org.example.Services;

import org.example.Entity.Estudiante;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

import java.util.ArrayList;

import org.example.DTOS.EstudianteDTO;

public class EstudianteService {

    private final RepositoryFactory factory;

    public EstudianteService() {
        this.factory = JPARepositoryFactory.getInstance();
    }

    public EstudianteDTO altaEstudiante(Estudiante estudiante) {

        if (factory.getEstudianteRepository().existePorDni(estudiante.getDni())) {
            throw new IllegalArgumentException("Ya existe un estudiante con ese DNI");
        }


        factory.getEstudianteRepository().persist(estudiante);

        return new EstudianteDTO(
                estudiante.getNroLegajo(),
                estudiante.getNombres(),
                estudiante.getApellido(),
                estudiante.getDni()
        );
    }

    public ArrayList<EstudianteDTO> obtenerEstudiantesOrderByLegajo(){
        ArrayList<EstudianteDTO> estudiantes = new ArrayList<>();

        for (Estudiante estudiante : factory.getEstudianteRepository().obtenerEstudiantesOrderByLegajo()) {
            estudiantes.add(new EstudianteDTO(
                    estudiante.getNroLegajo(),
                    estudiante.getNombres(),
                    estudiante.getApellido(),
                    estudiante.getDni()
            ));
        }

        return estudiantes;
    }

    public void eliminarEstudiante(Integer nroLegajo) {
        factory.getEstudianteRepository().delete(nroLegajo);
    }
}
