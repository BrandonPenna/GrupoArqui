package org.example.Factory;

public class JPARepositoryFactory implements RepositoryFactory {

    private static JPARepositoryFactory instance;

    // Constructor privado para el Singleton de la Fábrica
    private JPARepositoryFactory() {}

    public static JPARepositoryFactory getInstance() {
        if (instance == null) {
            instance = new JPARepositoryFactory();
        }
        return instance;
    }

    @Override
    public EstudianteRepository getEstudianteRepository() {
        return EstudianteRepository.getInstance();
    }

    @Override
    public CarreraRepository getCarreraRepository() {
        return CarreraRepository.getInstance();
    }

    @Override
    public UniversidadRepository getUniversidadRepository() {
        return UniversidadRepository.getInstance();
    }

    @Override
    public InscripcionRepository getInscripcionRepository() {
        return InscripcionRepository.getInstance();
    }
}
