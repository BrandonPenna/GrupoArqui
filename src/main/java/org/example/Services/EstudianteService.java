package org.example.Services;

import org.example.DTOS.CreateEstudianteRequest;
import org.example.DTOS.EstudianteResponse;
import org.example.Entity.Estudiante;
import org.example.Repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudiante;

    public EstudianteService(EstudianteRepository estudiante) {
        this.estudiante = estudiante;
    }

    public EstudianteResponse darAlta(CreateEstudianteRequest request){
        if (request.nroLegajo() == null || request.dni() == null || request.nombres() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del estudiante");
        }
        if (estudiante.existsById(request.nroLegajo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un estudiante con ese legajo");
        }
        if (estudiante.existsByDni(request.dni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un estudiante con ese DNI");
        }
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

    public List<EstudianteResponse> getAllOrderByLegajo() {
        return estudiante.findAll(Sort.by(Sort.Direction.ASC, "nroLegajo"))
            .stream()
                .map(item -> new EstudianteResponse(
                        item.getNroLegajo(),
                        item.getNombres(),
                        item.getApellido(),
                        item.getEdad(),
                        item.getGenero(),
                        item.getDni(),
                        item.getCiudadResidencia()
                ))
            .toList();
    }
    
    public EstudianteResponse getEstudianteByLegajo(Integer nroLegajo) {
        Estudiante estudianteEntity = estudiante.findById(nroLegajo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado"));

        return new EstudianteResponse(
                estudianteEntity.getNroLegajo(),
                estudianteEntity.getNombres(),
                estudianteEntity.getApellido(),
                estudianteEntity.getEdad(),
                estudianteEntity.getGenero(),
                estudianteEntity.getDni(),
                estudianteEntity.getCiudadResidencia()
        );
    }

    public List<EstudianteResponse> getAllByGenero(String genero) {
        return estudiante.findAllByGeneroIgnoreCaseOrderByNroLegajoAsc(genero)
                .stream()
                .map(item -> new EstudianteResponse(
                        item.getNroLegajo(),
                        item.getNombres(),
                        item.getApellido(),
                        item.getEdad(),
                        item.getGenero(),
                        item.getDni(),
                        item.getCiudadResidencia()
                ))
                .toList();
    }

    public List<EstudianteResponse> getByCarreraYCiudad(Integer idCarrera, String ciudad) {
        return estudiante.obtenerPorCarreraYCiudad(idCarrera, ciudad)
                .stream()
                .map(item -> new EstudianteResponse(
                        item.getNroLegajo(),
                        item.getNombres(),
                        item.getApellido(),
                        item.getEdad(),
                        item.getGenero(),
                        item.getDni(),
                        item.getCiudadResidencia()
                ))
                .toList();
    }
}
