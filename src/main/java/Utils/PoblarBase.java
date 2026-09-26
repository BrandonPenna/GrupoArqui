package Utils;

import org.example.Entity.Estudiante;
import org.example.Entity.Universidad;
import org.example.Entity.Inscripcion;
import org.example.DTOS.InscripcionId;
import org.example.Entity.Carrera;
import org.example.Factory.JPARepositoryFactory;
import org.example.Factory.RepositoryFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PoblarBase {

    public static void cargarDatos() {
        Set<InscripcionId> procesadas = new HashSet<>();
        RepositoryFactory factory = JPARepositoryFactory.getInstance();

        // 1. Verificar o crear la Universidad por defecto (Idempotente)
        Universidad universidad = factory.getUniversidadRepository().findAll().stream()
                .filter(u -> u.getNombre().equals("UNICEN"))
                .findFirst()
                .orElse(null);

        if (universidad == null) {
            universidad = new Universidad("UNICEN");
            factory.getUniversidadRepository().persist(universidad);
            System.out.println("-> Universidad creada con éxito.");
        } else {
            System.out.println("-> La universidad ya existía en la base.");
        }

        // 2. Cargar Carreras desde 'carreras.csv' (Idempotente)
        String carrerasFile = "src/main/java/Utils/carreras.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(carrerasFile))) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Integer idCarrera = Integer.parseInt(data[0].trim());
                String nombreCarrera = data[1].trim();
                Integer duracion = Integer.parseInt(data[2].trim());

                // Verificar si la carrera ya existe antes de persistir
                Carrera carreraExistente = factory.getCarreraRepository().findById(idCarrera);
                if (carreraExistente == null) {
                    Carrera carrera = new Carrera();
                    carrera.setIdCarrera(idCarrera);
                    carrera.setNombre(nombreCarrera);
                    carrera.setDuracion(duracion);
                    carrera.setUniversidad(universidad);

                    factory.getCarreraRepository().persist(carrera);
                }
            }
            System.out.println("-> Carreras procesadas correctamente desde el CSV.");
        } catch (Exception e) {
            System.err.println("Error cargando carreras: " + e.getMessage());
        }

        // 3. Cargar Estudiantes desde 'estudiantes.csv' (Idempotente)
        String estudiantesFile = "src/main/java/Utils/estudiantes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(estudiantesFile, java.nio.charset.StandardCharsets.ISO_8859_1))) {
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
                if (!factory.getEstudianteRepository().existePorLegajo(nroLegajo)) {
                    Estudiante estudiante = new Estudiante(nroLegajo, nombre, apellido, fechaNacimiento, genero, ciudad, dni);
                    factory.getEstudianteRepository().persist(estudiante);
                }
            }
            System.out.println("-> Estudiantes procesados correctamente desde el CSV.");
        } catch (Exception e) {
            System.err.println("Error cargando estudiantes: " + e.getMessage());
        }

        // 4. Cargar Inscripciones desde 'estudianteCarrera.csv' (Ya era idempotente)
        String inscripcionesFile = "src/main/java/Utils/estudianteCarrera.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(inscripcionesFile))) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                Integer idEstudianteDni = Integer.parseInt(data[1].trim());
                Integer idCarrera = Integer.parseInt(data[2].trim());
                Integer anioInscripcion = Integer.parseInt(data[3].trim());
                Integer anioGraduacion = Integer.parseInt(data[4].trim());
                boolean graduado = anioGraduacion > 0;

                Estudiante estudianteBD = factory.getEstudianteRepository().findAll().stream()
                        .filter(e -> e.getDni().equals(idEstudianteDni))
                        .findFirst()
                        .orElse(null);

                if (estudianteBD != null) {
                    InscripcionId idCompuesto = new InscripcionId(estudianteBD.getNroLegajo(), idCarrera);

                    if (!procesadas.contains(idCompuesto)) {
                        Inscripcion existente = factory.getInscripcionRepository().findById(idCompuesto);

                        if (existente == null) {
                            Inscripcion inscripcion = new Inscripcion();
                            inscripcion.setNroLegajo(estudianteBD.getNroLegajo());
                            inscripcion.setIdCarrera(idCarrera);
                            inscripcion.setAnioInscripcion(anioInscripcion);
                            inscripcion.setAnioGraduacion(anioGraduacion);
                            inscripcion.setGraduado(graduado);

                            factory.getInscripcionRepository().persist(inscripcion);
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
}