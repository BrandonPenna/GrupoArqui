package org.example.Controller;

import org.example.DTOS.CreateEstudianteRequest;
import org.example.DTOS.EstudianteResponse;
import org.example.Services.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
