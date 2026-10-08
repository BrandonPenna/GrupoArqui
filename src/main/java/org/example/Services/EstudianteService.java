package org.example.Services;

import org.example.DTOS.CreateEstudianteRequest;
import org.example.DTOS.EstudianteResponse;
import org.example.Entity.Estudiante;
import org.example.Repository.EstudianteRepository;
import org.springframework.stereotype.Service;

@Service
public class EstudianteService {

    private final EstudianteRepository estudiante;

    public EstudianteService(EstudianteRepository estudiante) {
        this.estudiante = estudiante;
    }

    public EstudianteResponse darAlta(CreateEstudianteRequest request){
        Estudiante nuevo = new Estudiante();
        nuevo.setNombres(request.nombres());
        nuevo.setApellido(request.apellido());
        nuevo.setFechaNacimiento(request.fechaNacimiento());
        nuevo.setGenero(request.genero());
        nuevo.setDni(request.dni());
        nuevo.setCiudadResidencia(request.ciudadResidencia());
        nuevo.setNroLegajo(request.nroLegajo());

        Estudiante guardado = estudiante.save(nuevo);

        return new EstudianteResponse(
                guardado.getNroLegajo(),
                guardado.getNombres(),
                guardado.getApellido(),
                guardado.getEdad(),
                guardado.getGenero(),
                guardado.getDni(),
                guardado.getCiudadResidencia()
        );
    }
}
