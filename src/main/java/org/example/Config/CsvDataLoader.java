package org.example.Config;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.Entity.Carrera;
import org.example.Entity.Estudiante;
import org.example.Entity.Inscripcion;
import org.example.Repository.CarreraRepository;
import org.example.Repository.EstudianteRepository;
import org.example.Repository.InscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvDataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataLoader.class);

    private final CarreraRepository carreraRepository;
    private final EstudianteRepository estudianteRepository;
    private final InscripcionRepository inscripcionRepository;

    public CsvDataLoader(CarreraRepository carreraRepository,
                         EstudianteRepository estudianteRepository,
                         InscripcionRepository inscripcionRepository) {
        this.carreraRepository = carreraRepository;
        this.estudianteRepository = estudianteRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        int carreras = cargarCarreras();
        int estudiantes = cargarEstudiantes();
        int inscripciones = cargarInscripciones();

        logger.info("Carga CSV finalizada: {} carreras, {} estudiantes y {} inscripciones nuevas",
                carreras, estudiantes, inscripciones);
    }

    private int cargarCarreras() throws IOException {
        int cargadas = 0;
        for (CSVRecord row : leerCsv("Utils/carreras.csv")) {
            Integer id = entero(row, "id_carrera");
            if (carreraRepository.existsById(id)) {
                continue;
            }

            Carrera carrera = new Carrera(row.get("carrera").trim(), entero(row, "duracion"));
            carrera.setIdCarrera(id);
            carreraRepository.save(carrera);
            cargadas++;
        }
        return cargadas;
    }

    private int cargarEstudiantes() throws IOException {
        int cargados = 0;
        for (CSVRecord row : leerCsv("Utils/estudiantes.csv")) {
            Integer legajo = entero(row, "LU");
            Integer dni = entero(row, "DNI");
            if (estudianteRepository.existsById(legajo) || estudianteRepository.existsByDni(dni)) {
                continue;
            }

            Estudiante estudiante = new Estudiante(
                    legajo,
                    row.get("nombre").trim(),
                    row.get("apellido").trim(),
                    entero(row, "edad"),
                    row.get("genero").trim(),
                    row.get("ciudad").trim(),
                    dni);
            estudianteRepository.save(estudiante);
            cargados++;
        }
        return cargados;
    }

    private int cargarInscripciones() throws IOException {
        int cargadas = 0;
        for (CSVRecord row : leerCsv("Utils/estudianteCarrera.csv")) {
            Integer dni = entero(row, "id_estudiante");
            Integer idCarrera = entero(row, "id_carrera");
            Integer anioInscripcion = entero(row, "inscripcion");
            Integer anioGraduacion = entero(row, "graduacion");
            Integer antiguedad = entero(row, "antiguedad");

            if (!estudianteRepository.existsByDni(dni) || !carreraRepository.existsById(idCarrera)) {
                logger.warn("Se omite la inscripción CSV {}: no existe el estudiante DNI {} o la carrera {}",
                        row.get("id"), dni, idCarrera);
                continue;
            }

            if (inscripcionRepository.existsByIdEstudianteAndIdCarreraAndAnioInscripcionAndAnioGraduacionAndAntiguedad(
                    dni, idCarrera, anioInscripcion, anioGraduacion, antiguedad)) {
                continue;
            }

            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setIdEstudiante(dni);
            inscripcion.setIdCarrera(idCarrera);
            inscripcion.setAnioInscripcion(anioInscripcion);
            inscripcion.setAnioGraduacion(anioGraduacion);
            inscripcion.setAntiguedad(antiguedad);
            inscripcionRepository.save(inscripcion);
            cargadas++;
        }
        return cargadas;
    }

    private List<CSVRecord> leerCsv(String ruta) throws IOException {
        try (Reader reader = new InputStreamReader(
                new ClassPathResource(ruta).getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {
            return parser.getRecords();
        }
    }

    private Integer entero(CSVRecord row, String columna) {
        return Integer.valueOf(row.get(columna).trim());
    }
}
