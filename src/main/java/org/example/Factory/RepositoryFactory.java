package org.example.Factory;

public interface RepositoryFactory {
    EstudianteRepository getEstudianteRepository();
    CarreraRepository getCarreraRepository();
    UniversidadRepository getUniversidadRepository();
    InscripcionRepository getInscripcionRepository();
}
