package org.example;
import org.example.DTOS.*;
import org.example.Entity.*;
import org.example.Services.*;
import Utils.PoblarBase;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        EstudianteService estudianteService = new EstudianteService();
        InscripcionService inscripcionService = new InscripcionService();
        CarreraService carreraService = new CarreraService();
        UniversidadService universidadService = new UniversidadService();


        //PoblarBase.cargarDatos();

        //2A) Dar de alta a un estudiante
        /*estudianteService.eliminarEstudiante(1001);

        Estudiante estudiante = new Estudiante(
                1001,
                "Juan",
                "Perez",
                LocalDate.of(2000, 5, 10),
                "Masculino",
                "Tandil",
                40123456
        );

        EstudianteDTO estudianteDTO = estudianteService.altaEstudiante(estudiante);
        System.out.println(estudianteDTO);*/


        //2B) Matricular a un estudiante en una carrera

        /*Elimino inscripcion para evitar conflictos
        inscripcionService.eliminarInscripcion(34978, 1);
        Inscripcion inscripcion = inscripcionService.matricular(
                34978, 1, 2024
        );

        System.out.println("Matriculado: " + inscripcion.getNroLegajo() + " en carrera " + inscripcion.getIdCarrera()
        + " - Año de inscripción: " + inscripcion.getAnioInscripcion() + " - Graduado: " + inscripcion.getGraduado());*/


        //2C) Recuperar todos los estudiantes y especificar un ordenamiento simple (legajo ascendente))

        /*List<EstudianteDTO> estudiantesOrdenados = estudianteService.obtenerEstudiantesOrderByLegajo();
        System.out.println("Estudiantes ordenados por legajo:");
        for (EstudianteDTO e : estudiantesOrdenados) {
            System.out.println(e);
        }*/

        //2D) Recuperar un estudiante por su número de legajo

        /*Integer legajoBuscado = 10383;
        EstudianteDTO estudianteEncontrado = estudianteService.obtenerEstudiantePorLegajo(legajoBuscado);

        if (estudianteEncontrado != null) {
            System.out.println("Estudiante encontrado por legajo " + legajoBuscado + ":");
            System.out.println(estudianteEncontrado);
        } else {
            System.out.println("No se encontró ningún estudiante con el legajo: " + legajoBuscado);
        }*/

        //2E)recuperar todos los estudiantes, en base a su género.
        /*String generoBuscado = "Male";
        List<EstudianteDTO> estudiantesPorGenero = estudianteService.obtenerEstudiantesPorGenero(generoBuscado);

        System.out.println("Estudiantes de género '" + generoBuscado + "':");
        for (EstudianteDTO e : estudiantesPorGenero) {
            System.out.println(e);
        }*/

        // 2F) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos
        /*List<CarreraInscriptosDTO> carrerasConInscriptos = carreraService.obtenerCarrerasConInscriptosOrdenadas();

        System.out.println("--- Carreras ordenadas por cantidad de inscriptos ---");
        for (CarreraInscriptosDTO c : carrerasConInscriptos) {
            System.out.println(c);
        }*/

        // 2G) Recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia
        /*Integer idCarreraBuscada = 1;
        String ciudadBuscada = "Sungairaya";

        List<EstudianteDTO> estudiantesCarreraCiudad = estudianteService
                .obtenerEstudiantesPorCarreraYCiudad(idCarreraBuscada, ciudadBuscada);

        System.out.println("--- Estudiantes de la carrera " + idCarreraBuscada + " que viven en " + ciudadBuscada + " ---");
        for (EstudianteDTO e : estudiantesCarreraCiudad) {
            System.out.println(e);
        }*/
    }
}