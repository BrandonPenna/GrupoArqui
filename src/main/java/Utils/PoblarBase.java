package Utils;

import org.example.Entity.Estudiante;
import org.example.Entity.Universidad;
import org.example.Entity.Inscripcion;
import org.example.Entity.InscripcionId;
import org.example.Entity.Carrera;
import org.example.Repository.CarreraRepository;
import org.example.Repository.EstudianteRepository;
import org.example.Repository.InscripcionRepository;
import org.example.Repository.UniversidadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;

@Component
public class PoblarBase implements CommandLineRunner {

    private final CarreraRepository carreraRepository;
    private final EstudianteRepository estudianteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UniversidadRepository universidadRepository;

    public PoblarBase(
            CarreraRepository carreraRepository,
            EstudianteRepository estudianteRepository,
            InscripcionRepository inscripcionRepository,
            UniversidadRepository universidadRepository
    ) {
        this.carreraRepository = carreraRepository;
        this.estudianteRepository = estudianteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.universidadRepository = universidadRepository;
    }

    @Override
    public void run(String... args) {
        cargarDatos();
    }

    public void cargarDatos() {
        Set<InscripcionId> procesadas = new HashSet<>();

        // 1. Verificar o crear la Universidad por defecto (Idempotente)
        Universidad universidad = universidadRepository.findAll().stream()
                .filter(u -> u.getNombre().equals("UNICEN"))
                .findFirst()
                .orElse(null);

        if (universidad == null) {
            universidad = new Universidad("UNICEN");
            universidad = universidadRepository.save(universidad);
            System.out.println("-> Universidad creada con éxito.");
        } else {
            System.out.println("-> La universidad ya existía en la base.");
        }

        // 2. Cargar Carreras desde 'carreras.csv' (Idempotente)
        try (BufferedReader br = abrirCsv("carreras.csv")) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Integer idCarrera = Integer.parseInt(data[0].trim());
                String nombreCarrera = data[1].trim();
                Integer duracion = Integer.parseInt(data[2].trim());

                // Verificar si la carrera ya existe antes de persistir
                if (carreraRepository.findById(idCarrera).isEmpty()) {
                    Carrera carrera = new Carrera();
                    carrera.setIdCarrera(idCarrera);
                    carrera.setNombre(nombreCarrera);
                    carrera.setDuracion(duracion);
                    carrera.setUniversidad(universidad);

                    carreraRepository.save(carrera);
                }
            }
            System.out.println("-> Carreras procesadas correctamente desde el CSV.");
        } catch (Exception e) {
            System.err.println("Error cargando carreras: " + e.getMessage());
        }

        // 3. Cargar Estudiantes desde 'estudiantes.csv' (Idempotente)
        try (BufferedReader br = abrirCsv("estudiantes.csv", StandardCharsets.ISO_8859_1)) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Integer dni = Integer.parseInt(data[0].trim());
                String nombre = data[1].trim();
                String apellido = data[2].trim();
                int edadValor = Integer.parseInt(data[3].trim());
                String genero = data[4].trim();
                String ciudad = data[5].trim();
                Integer nroLegajo = Integer.parseInt(data[6].trim());

                LocalDate fechaNacimiento = LocalDate.now().minusYears(edadValor);

                // Verificar si el estudiante ya existe por su legajo
                if (!estudianteRepository.existsById(nroLegajo)) {
                    Estudiante estudiante = new Estudiante(nroLegajo, nombre, apellido, fechaNacimiento, genero, ciudad, dni);
                    estudianteRepository.save(estudiante);
                }
            }
            System.out.println("-> Estudiantes procesados correctamente desde el CSV.");
        } catch (Exception e) {
            System.err.println("Error cargando estudiantes: " + e.getMessage());
        }

        // 4. Cargar Inscripciones desde 'estudianteCarrera.csv' (Ya era idempotente)
        try (BufferedReader br = abrirCsv("estudianteCarrera.csv")) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                Integer idEstudianteDni = Integer.parseInt(data[1].trim());
                Integer idCarrera = Integer.parseInt(data[2].trim());
                Integer anioInscripcion = Integer.parseInt(data[3].trim());
                Integer anioGraduacion = Integer.parseInt(data[4].trim());
                boolean graduado = anioGraduacion > 0;

                Estudiante estudianteBD = estudianteRepository.findAll().stream()
                        .filter(e -> e.getDni().equals(idEstudianteDni))
                        .findFirst()
                        .orElse(null);

                if (estudianteBD != null) {
                    InscripcionId idCompuesto = new InscripcionId(estudianteBD.getNroLegajo(), idCarrera);

                    if (!procesadas.contains(idCompuesto)) {
                        if (!inscripcionRepository.existsById(idCompuesto)) {
                            Inscripcion inscripcion = new Inscripcion();
                            inscripcion.setNroLegajo(estudianteBD.getNroLegajo());
                            inscripcion.setIdCarrera(idCarrera);
                            inscripcion.setAnioInscripcion(anioInscripcion);
                            inscripcion.setAnioGraduacion(anioGraduacion);
                            inscripcion.setGraduado(graduado);

                            inscripcionRepository.save(inscripcion);
                            procesadas.add(idCompuesto);
                        }
                    }
                }
            }
            System.out.println("-> Inscripciones procesadas exitosamente sin duplicados.");
        } catch (Exception e) {
            System.err.println("Error cargando inscripciones: " + e.getMessage());
        }
    }

    private BufferedReader abrirCsv(String nombreArchivo) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new ClassPathResource("Utils/" + nombreArchivo).getInputStream()
        ));
    }

    private BufferedReader abrirCsv(String nombreArchivo, Charset charset) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new ClassPathResource("Utils/" + nombreArchivo).getInputStream(),
                charset
        ));
    }
}