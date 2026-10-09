package org.example.Entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "inscripcion")
public class Inscripcion implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // El CSV relaciona al estudiante mediante su DNI, no mediante LU.
    @Column(name = "id_estudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "id_carrera", nullable = false)
    private Integer idCarrera;

    @Column(name = "inscripcion", nullable = false)
    private Integer anioInscripcion;

    @Column(name = "graduacion", nullable = false)
    private Integer anioGraduacion;

    @Column(name = "antiguedad", nullable = false)
    private Integer antiguedad;

    @ManyToOne
    @JoinColumn(name = "id_estudiante", referencedColumnName = "DNI", insertable = false, updatable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_carrera", referencedColumnName = "id_carrera", insertable = false, updatable = false)
    private Carrera carrera;

    public Inscripcion() {}

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getIdEstudiante() { return idEstudiante; }

    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }

    public Integer getNroLegajo() { return idEstudiante; }

    public void setNroLegajo(Integer nroLegajo) { this.idEstudiante = nroLegajo; }

    public Integer getIdCarrera() { return idCarrera; }

    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }

    public Integer getAnioInscripcion() { return anioInscripcion; }

    public void setAnioInscripcion(Integer anioInscripcion) { this.anioInscripcion = anioInscripcion; }

    public Integer getAnioGraduacion() { return anioGraduacion; }

    public void setAnioGraduacion(Integer anioGraduacion) { this.anioGraduacion = anioGraduacion; }

    public Integer getAntiguedad() { return antiguedad; }

    public void setAntiguedad(Integer antiguedad) { this.antiguedad = antiguedad; }

    public Boolean getGraduado() { return anioGraduacion != null && anioGraduacion > 0; }

    public Carrera getCarrera() { return carrera; }

    public void setCarrera(Carrera carrera) { this.carrera = carrera; }

    public Estudiante getEstudiante() { return estudiante; }

    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
}
