package org.example.Factory;

import org.example.Repository.CarreraRepository;
import org.example.Repository.EstudianteRepository;
import org.example.Repository.InscripcionRepository;
import org.example.Repository.UniversidadRepository;

public interface RepositoryFactory {
    EstudianteRepository getEstudianteRepository();
    CarreraRepository getCarreraRepository();
    UniversidadRepository getUniversidadRepository();
    InscripcionRepository getInscripcionRepository();
}
