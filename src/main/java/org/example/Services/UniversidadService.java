package org.example.Services;

import org.example.Entity.Universidad;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

public class UniversidadService {
    private final RepositoryFactory factory;

    public UniversidadService() {
        this.factory = JPARepositoryFactory.getInstance();
    }

    public Universidad altaUniversidad(Universidad universidad) {
        if (factory.getUniversidadRepository().existePorId(universidad.getIdUniversidad())) {
            throw new IllegalArgumentException("Ya existe una universidad con ese ID");
        }

        return factory.getUniversidadRepository().persist(universidad);
    }

    public void eliminarUniversidad(Integer idUniversidad) {
        factory.getUniversidadRepository().delete(idUniversidad);
    }
}
