package org.example.Services;

import org.example.Entity.Universidad;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;
import org.example.Repository.UniversidadRepository;

public class UniversidadService {
    private static UniversidadService instance;
    private final UniversidadRepository universidadRepository;



    private UniversidadService() {
        this.universidadRepository = UniversidadRepository.getInstance();
    }


    public static UniversidadService getInstance(){
        if(instance==null){
            instance= new UniversidadService();
        }
        return instance;
    }

    public Universidad altaUniversidad(Universidad universidad) {
        if (this.universidadRepository.existePorId(universidad.getIdUniversidad())) {
            throw new IllegalArgumentException("Ya existe una universidad con ese ID");
        }

        return this.universidadRepository.persist(universidad);
    }

    public void eliminarUniversidad(Integer idUniversidad) {
        this.universidadRepository.delete(idUniversidad);
    }
}
