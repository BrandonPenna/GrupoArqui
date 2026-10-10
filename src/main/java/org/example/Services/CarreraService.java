package org.example.Services;

import java.util.List;
import org.example.DTOS.CarreraResponse;
import org.example.DTOS.CreateCarreraRequest;
import org.example.DTOS.ReporteCarreraResponse;
import org.example.DTOS.CarreraInscriptosResponse;
import org.example.Entity.Carrera;
import org.example.Repository.CarreraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public CarreraService(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    public CarreraResponse darAlta(CreateCarreraRequest request) {
        if (request == null || request.idCarrera() == null || request.idCarrera() <= 0
                || request.nombre() == null || request.nombre().isBlank()
                || request.duracion() == null || request.duracion() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El id, el nombre y la duración de la carrera son obligatorios y deben ser válidos");
        }
        if (carreraRepository.existsById(request.idCarrera())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una carrera con ese id");
        }

        Carrera nueva = new Carrera(request.nombre().trim(), request.duracion());
        nueva.setIdCarrera(request.idCarrera());
        Carrera guardada = carreraRepository.save(nueva);

        return new CarreraResponse(guardada.getIdCarrera(), guardada.getNombre(), guardada.getDuracion());
    }

    public List<CarreraInscriptosResponse> getCarrerasOrdenadasPorInscriptos() {
        return carreraRepository.obtenerCarrerasConInscriptosOrdenadas();
    }

    public List<ReporteCarreraResponse> getReporteCarreras() {
        return carreraRepository.obtenerReporteCarreras()
                .stream()
                .map(fila -> new ReporteCarreraResponse(
                        (String) fila[0],
                        ((Number) fila[1]).intValue(),
                        ((Number) fila[2]).longValue(),
                        ((Number) fila[3]).longValue()
                ))
                .toList();
    }
}
