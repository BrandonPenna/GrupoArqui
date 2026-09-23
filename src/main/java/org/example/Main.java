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
    }
}