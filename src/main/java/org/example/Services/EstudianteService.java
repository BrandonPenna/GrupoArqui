package org.example.Services;

import org.example.Entity.Estudiante;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

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

    public EstudianteDTO obtenerEstudiantePorLegajo(Integer nroLegajo) {
        Estudiante estudiante = factory.getEstudianteRepository().obtenerPorLegajo(nroLegajo);
        if (estudiante == null) {
            return null;
        }
        return new EstudianteDTO(
                estudiante.getNroLegajo(),
                estudiante.getNombres(),
                estudiante.getApellido(),
                estudiante.getDni()
        );
    }

    public ArrayList<EstudianteDTO> obtenerEstudiantesPorGenero(String genero) {
        ArrayList<EstudianteDTO> estudiantesDTO = new ArrayList<>();
        List<Estudiante> estudiantes = factory.getEstudianteRepository().obtenerEstudiantesPorGenero(genero);

        for (Estudiante e : estudiantes) {
            estudiantesDTO.add(new EstudianteDTO(
                    e.getNroLegajo(),
                    e.getNombres(),
                    e.getApellido(),
                    e.getDni()
            ));
        }

        return estudiantesDTO;
    }

    public List<EstudianteDTO> obtenerEstudiantesPorCarreraYCiudad(Integer idCarrera, String ciudadResidencia) {
        List<EstudianteDTO> estudiantesDTO = new ArrayList<>();
        List<Estudiante> estudiantes = factory.getEstudianteRepository()
                .obtenerEstudiantesPorCarreraYCiudad(idCarrera, ciudadResidencia);

        for (Estudiante e : estudiantes) {
            estudiantesDTO.add(new EstudianteDTO(
                    e.getNroLegajo(),
                    e.getNombres(),
                    e.getApellido(),
                    e.getDni()
            ));
        }

        return estudiantesDTO;
    }

    public void eliminarEstudiante(Integer nroLegajo) {
        factory.getEstudianteRepository().delete(nroLegajo);
    }
}
