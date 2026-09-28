package org.example.Services;

import org.example.Entity.Estudiante;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import org.example.DTOS.EstudianteDTO;
import org.example.Repository.EstudianteRepository;

public class EstudianteService {
    private static EstudianteService instance;
    private final EstudianteRepository repoEstudiante;

    private EstudianteService() {
        this.repoEstudiante = EstudianteRepository.getInstance();
    }
    public static EstudianteService getInstance(){
        if(instance==null){
            instance= new EstudianteService();
        }
        return instance;
    }

    public EstudianteDTO altaEstudiante(Estudiante estudiante) {

        if (this.repoEstudiante.existePorDni(estudiante.getDni())) {
            throw new IllegalArgumentException("Ya existe un estudiante con ese DNI");
        }


        this.repoEstudiante.persist(estudiante);

        return new EstudianteDTO(
                estudiante.getNroLegajo(),
                estudiante.getNombres(),
                estudiante.getApellido(),
                estudiante.getDni()
        );
    }

    public ArrayList<EstudianteDTO> obtenerEstudiantesOrderByLegajo(){
        ArrayList<EstudianteDTO> estudiantes = new ArrayList<>();

        for (Estudiante estudiante : this.repoEstudiante.obtenerEstudiantesOrderByLegajo()) {
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
        Estudiante estudiante = this.repoEstudiante.obtenerPorLegajo(nroLegajo);
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
        List<Estudiante> estudiantes = this.repoEstudiante.obtenerEstudiantesPorGenero(genero);

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
        List<Estudiante> estudiantes = this.repoEstudiante.obtenerEstudiantesPorCarreraYCiudad(idCarrera, ciudadResidencia);

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
        this.repoEstudiante.delete(nroLegajo);
    }
}
