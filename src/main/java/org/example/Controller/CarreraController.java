package org.example.Controller;

import java.util.List;

import org.example.DTOS.CarreraResponse;
import org.example.DTOS.CarreraInscriptosResponse;
import org.example.DTOS.CreateCarreraRequest;
import org.example.DTOS.ReporteCarreraResponse;
import org.example.Services.CarreraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carreras")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @PostMapping
    public ResponseEntity<CarreraResponse> darAlta(@RequestBody CreateCarreraRequest request) {
        CarreraResponse carreraNueva = carreraService.darAlta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraNueva);
    }

    @GetMapping("/inscriptos")
    public ResponseEntity<List<CarreraInscriptosResponse>> getCarrerasOrdenadasPorInscriptos() {
        return ResponseEntity.ok(carreraService.getCarrerasOrdenadasPorInscriptos());
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<ReporteCarreraResponse>> getReporteCarreras() {
        return ResponseEntity.ok(carreraService.getReporteCarreras());
    }
}
