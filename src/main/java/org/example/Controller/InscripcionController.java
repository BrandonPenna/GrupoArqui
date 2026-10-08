package org.example.Controller;

import org.example.DTOS.InscripcionResponse;
import org.example.DTOS.MatricularEstudianteRequest;
import org.example.Services.InscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> matricular(@RequestBody MatricularEstudianteRequest request) {
        InscripcionResponse inscripcion = inscripcionService.matricular(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(inscripcion);
    }
}