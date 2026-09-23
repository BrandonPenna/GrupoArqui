package org.example.Services;
import org.example.DTOS.ReporteCarreraDTO;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
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

    public List<ReporteCarreraDTO> generarReporteCarreras() {
        List<Object[]> inscriptos = factory.getCarreraRepository().obtenerInscriptosPorAnio();
        List<Object[]> egresados = factory.getCarreraRepository().obtenerEgresadosPorAnio();

        // TreeMap mantiene las carreras ordenadas alfabéticamente y los años cronológicamente
        Map<String, Map<Integer, ReporteCarreraDTO>> mapa = new TreeMap<>();

        // 1. Cargamos los inscriptos
        for (Object[] fila : inscriptos) {
            String carrera = (String) fila[0];
            Integer anio = (Integer) fila[1];
            Long cantInscriptos = (Long) fila[2];

            mapa.putIfAbsent(carrera, new TreeMap<>());
            mapa.get(carrera).put(anio, new ReporteCarreraDTO(carrera, anio, cantInscriptos, 0L));
        }

        // 2. Cruzamos los egresados
        for (Object[] fila : egresados) {
            String carrera = (String) fila[0];
            Integer anio = (Integer) fila[1];
            Long cantEgresados = (Long) fila[2];

            mapa.putIfAbsent(carrera, new TreeMap<>());
            ReporteCarreraDTO reporte = mapa.get(carrera).get(anio);

            if (reporte != null) {
                reporte.setEgresados(cantEgresados);
            } else {
                // Si en ese año hubo egresados pero ningún inscripto nuevo
                mapa.get(carrera).put(anio, new ReporteCarreraDTO(carrera, anio, 0L, cantEgresados));
            }
        }

        // 3. Pasamos todo a una sola lista ya ordenada
        List<ReporteCarreraDTO> resultado = new ArrayList<>();
        for (Map<Integer, ReporteCarreraDTO> anios : mapa.values()) {
            resultado.addAll(anios.values());
        }

        return resultado;
    }
}
