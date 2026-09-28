package org.example.Services;
import org.example.DTOS.ReporteCarreraDTO;
import org.example.DTOS.CarreraInscriptosDTO;
import org.example.Entity.Carrera;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;
import org.example.Repository.CarreraRepository;

import java.util.List;

public class CarreraService {
    private static CarreraService instance;
    private final CarreraRepository carreraRepository;

    private CarreraService() {
        this.carreraRepository = CarreraRepository.getInstance();
    }

    public Carrera altaCarrera(Carrera carrera) {
        if(this.carreraRepository.existePorId(carrera.getIdCarrera())) {
            throw new IllegalArgumentException("Ya existe una carrera con ese ID");
        }
        
        return this.carreraRepository.persist(carrera);
    }

    public static CarreraService getInstance(){
        if(instance==null){
            instance= new CarreraService();
        }
        return instance;
    }

    public List<CarreraInscriptosDTO> obtenerCarrerasConInscriptosOrdenadas() {
        return this.carreraRepository.obtenerCarrerasConInscriptosOrdenadas();
    }

    public void eliminarCarrera(Integer idCarrera) {
        this.carreraRepository.delete(idCarrera);
    }

    // El reporte se genera completo (agrupado y ordenado) en la consulta SQL nativa
    public List<ReporteCarreraDTO> generarReporteCarreras() {
        return this.carreraRepository.obtenerReporteCarreras();
    }
}
