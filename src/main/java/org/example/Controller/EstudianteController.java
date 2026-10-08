package org.example.Controller;

import org.example.DTOS.CreateEstudianteRequest;
import org.example.DTOS.EstudianteResponse;
import org.example.Services.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @PostMapping
    public ResponseEntity<EstudianteResponse> darAlta(@RequestBody CreateEstudianteRequest request){
        EstudianteResponse estudianteNuevo = estudianteService.darAlta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(estudianteNuevo);
    }

    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> getAllOrderByLegajo() {
        return ResponseEntity.ok(estudianteService.getAllOrderByLegajo());
    }

    @GetMapping("/{nroLegajo}")
    public ResponseEntity<EstudianteResponse> getByLegajo(@PathVariable Integer nroLegajo) {
        return ResponseEntity.ok(estudianteService.getEstudianteByLegajo(nroLegajo));
    }

    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<EstudianteResponse>> getAllByGenero(@PathVariable String genero) {
        return ResponseEntity.ok(estudianteService.getAllByGenero(genero));
    }

    @GetMapping("/carrera/{idCarrera}")
    public ResponseEntity<List<EstudianteResponse>> getByCarreraYCiudad(@PathVariable Integer idCarrera,
                                                                        @RequestParam String ciudad) {
        return ResponseEntity.ok(estudianteService.getByCarreraYCiudad(idCarrera, ciudad));
    }

}
