package org.example.Services;


import org.example.DTOS.InscripcionResponse;
import org.example.DTOS.MatricularEstudianteRequest;
import org.example.Entity.Carrera;
import org.example.Entity.Inscripcion;
import org.example.Entity.InscripcionId;
import org.example.Repository.CarreraRepository;
import org.example.Repository.EstudianteRepository;
import org.example.Repository.InscripcionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InscripcionService {
	private final InscripcionRepository inscripcionRepository;
	private final EstudianteRepository estudianteRepository;
	private final CarreraRepository carreraRepository;

	public InscripcionService(
			InscripcionRepository inscripcionRepository,
			EstudianteRepository estudianteRepository,
			CarreraRepository carreraRepository
	) {
		this.inscripcionRepository = inscripcionRepository;
		this.estudianteRepository = estudianteRepository;
		this.carreraRepository = carreraRepository;
	}

	@Transactional
	public InscripcionResponse matricular(MatricularEstudianteRequest request) {
		if (request == null
				|| request.nroLegajo() == null || request.nroLegajo() <= 0
				|| request.idCarrera() == null || request.idCarrera() <= 0
				|| request.anioInscripcion() == null || request.anioInscripcion() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de matrícula son inválidos");
		}

		if (!estudianteRepository.existsById(request.nroLegajo())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el estudiante indicado");
		}

		Carrera carrera = carreraRepository.findById(request.idCarrera())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"No existe la carrera indicada"
				));

		InscripcionId id = new InscripcionId(request.nroLegajo(), request.idCarrera());
		if (inscripcionRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El estudiante ya está matriculado en esta carrera");
		}

		Inscripcion nueva = new Inscripcion();
		nueva.setNroLegajo(request.nroLegajo());
		nueva.setIdCarrera(request.idCarrera());
		nueva.setAnioInscripcion(request.anioInscripcion());
		nueva.setGraduado(false);
		nueva.setAnioGraduacion(null);

		Inscripcion guardada = inscripcionRepository.save(nueva);

		return new InscripcionResponse(
				guardada.getIdCarrera(),
				carrera.getNombre(),
				guardada.getAnioInscripcion(),
				guardada.getAnioGraduacion(),
				guardada.getGraduado()
		);
	}
}
