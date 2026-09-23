package org.example.Entity;
import jakarta.persistence.*;
import org.example.DTOS.InscripcionId;

import java.io.Serializable;

@Entity
@IdClass(InscripcionId.class)
public class Inscripcion implements Serializable {

    @Id
    @Column(name = "nro_legajo")
    private Integer nroLegajo;

    @Id
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(nullable = false)
    private Boolean graduado;

    @Column(nullable = false)
    private Integer anioInscripcion;

    @Column(name = "graduacion")
    private Integer anioGraduacion;

    @ManyToOne
    @JoinColumn(name = "nro_legajo", referencedColumnName = "nro_legajo", insertable = false, updatable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_carrera", referencedColumnName = "id_carrera", insertable = false, updatable = false)
    private Carrera carrera;

    public Inscripcion() {}

    public Inscripcion(Integer nroLegajo, Integer idCarrera, Boolean graduado) {
        this.nroLegajo = nroLegajo;
        this.idCarrera = idCarrera;
        this.graduado = graduado;
    }

    public Integer getNroLegajo() {
        return nroLegajo;
    }

    public void setNroLegajo(Integer nroLegajo) {
        this.nroLegajo = nroLegajo;
    }

    public Integer getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }

    public Boolean getGraduado() {
        return graduado;
    }

    public void setGraduado(Boolean graduado) {
        this.graduado = graduado;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Integer getAnioInscripcion() {
        return anioInscripcion;
    }

    public void setAnioInscripcion(Integer anioInscripcion) {
        this.anioInscripcion = anioInscripcion;
    }

    public Integer getAnioGraduacion() {
        return anioGraduacion;
    }

    public void setAnioGraduacion(Integer anioGraduacion) {
        this.anioGraduacion = anioGraduacion;
    }
}