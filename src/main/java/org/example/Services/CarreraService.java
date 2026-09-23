package org.example.Services;

import org.example.DTOS.CarreraInscriptosDTO;
import org.example.Entity.Carrera;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

import java.util.List;

public class CarreraService {
    private final RepositoryFactory factory;

    public CarreraService() {
        this.factory = JPARepositoryFactory.getInstance();
    }

    public Carrera altaCarrera(Carrera carrera) {
        if(factory.getCarreraRepository().existePorId(carrera.getIdCarrera())) {
            throw new IllegalArgumentException("Ya existe una carrera con ese ID");
        }
        
        return factory.getCarreraRepository().persist(carrera);
    }

    public List<CarreraInscriptosDTO> obtenerCarrerasConInscriptosOrdenadas() {
        return factory.getCarreraRepository().obtenerCarrerasConInscriptosOrdenadas();
    }
    public void eliminarCarrera(Integer idCarrera) {
        factory.getCarreraRepository().delete(idCarrera);
    }
}
