package org.example.Services;

import java.util.List;
import org.example.DTOS.ReporteCarreraResponse;
import org.example.DTOS.CarreraInscriptosResponse;
import org.example.Repository.CarreraRepository;
import org.springframework.stereotype.Service;

@Service
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public CarreraService(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
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