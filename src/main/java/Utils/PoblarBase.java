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
        Set procesadas = new HashSet<>();
        RepositoryFactory factory = JPARepositoryFactory.getInstance();

        // 1. Crear y persistir una Universidad por defecto (para vincular las carreras)
        Universidad universidad = new Universidad("UNICEN");
        factory.getUniversidadRepository().persist(universidad);
        System.out.println("-> Universidad creada con éxito.");

        // 2. Cargar Carreras desde 'carreras.csv'
        // Columnas en CSV: id_carrera, carrera, duracion
        String carrerasFile = "src/main/java/Utils/carreras.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(carrerasFile))) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Integer idCarrera = Integer.parseInt(data[0].trim());
                String nombreCarrera = data[1].trim();
                Integer duracion = Integer.parseInt(data[2].trim());

                Carrera carrera = new Carrera();
                carrera.setIdCarrera(idCarrera);
                carrera.setNombre(nombreCarrera);
                carrera.setDuracion(duracion);
                carrera.setUniversidad(universidad); // Asignamos la FK de la universidad

                factory.getCarreraRepository().persist(carrera);
            }
            System.out.println("-> Carreras cargadas correctamente desde el CSV (con su duración y universidad).");
        } catch (Exception e) {
            System.err.println("Error cargando carreras: " + e.getMessage());
        }

        // 3. Cargar Estudiantes desde 'estudiantes.csv'
        // Columnas en CSV: DNI, nombre, apellido, edad, genero, ciudad, LU
        String estudiantesFile = "src/main/java/Utils/estudiantes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(estudiantesFile, java.nio.charset.StandardCharsets.ISO_8859_1))) {
            String line = br.readLine(); // Saltar cabecera
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Integer dni = Integer.parseInt(data[0].trim());
                String nombre = data[1].trim();
                String apellido = data[2].trim();
                int edadValor = Integer.parseInt(data[3].trim()); // El CSV trae la edad o años
                String genero = data[4].trim();
                String ciudad = data[5].trim();
                Integer nroLegajo = Integer.parseInt(data[6].trim());

                // Mapeamos la edad a fecha de nacimiento aproximada para cumplir con el tipo LocalDate
                LocalDate fechaNacimiento = LocalDate.now().minusYears(edadValor);

                Estudiante estudiante = new Estudiante(nroLegajo, nombre, apellido, fechaNacimiento, genero, ciudad, dni);
                factory.getEstudianteRepository().persist(estudiante);
            }
            System.out.println("-> Estudiantes cargados correctamente desde el CSV.");
        } catch (Exception e) {
            System.err.println("Error cargando estudiantes: " + e.getMessage());
        }

        // 4. Cargar Inscripciones desde 'estudianteCarrera.csv'
        // Columnas en CSV: id, id_estudiante, id_carrera, inscripcion, graduacion, antiguedad
        String inscripcionesFile = "src/main/java/Utils/estudianteCarrera.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(inscripcionesFile))) {
            
            String line = br.readLine(); // Saltar cabecera
    while ((line = br.readLine()) != null) {
        String[] data = line.split(",");
        
        Integer idEstudianteDni = Integer.parseInt(data[1].trim());
        Integer idCarrera = Integer.parseInt(data[2].trim());
        Integer anioInscripcion = Integer.parseInt(data[3].trim());
        boolean graduado = !data[4].trim().isEmpty();

        Estudiante estudianteBD = factory.getEstudianteRepository().findAll().stream()
                .filter(e -> e.getDni().equals(idEstudianteDni))
                .findFirst()
                .orElse(null);

        if (estudianteBD != null) {
            InscripcionId idCompuesto = new InscripcionId(estudianteBD.getNroLegajo(), idCarrera);

            // 🔑 1. Evitar duplicados del mismo CSV en la sesión actual
            if (!procesadas.contains(idCompuesto)) {
                
                // 🔑 2. Verificar si ya existe en la base de datos
                Inscripcion existente = factory.getInscripcionRepository().findById(idCompuesto);
                
                if (existente == null) {
                    Inscripcion inscripcion = new Inscripcion();
                    inscripcion.setNroLegajo(estudianteBD.getNroLegajo());
                    inscripcion.setIdCarrera(idCarrera);
                    inscripcion.setAnioInscripcion(anioInscripcion);
                    inscripcion.setGraduado(graduado);

                    factory.getInscripcionRepository().persist(inscripcion);
                    try {
                        procesadas.add(idCompuesto);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
    System.out.println("-> Inscripciones cargadas exitosamente sin duplicados.");
} catch (Exception e) {
    System.err.println("Error cargando inscripciones: " + e.getMessage());

        }
            
    }
}
