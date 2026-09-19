package com.tuuniversidad.model;

import jakarta.persistence.*;
import org.example.Entity.Inscripcion;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "estudiante")
public class Estudiante implements Serializable {

    @Id
    @Column(name = "nro_legajo")
    private Integer nroLegajo;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellido;

    @Column(name = "edad", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private String genero;

    @Column(name = "ciudad_residencia", nullable = false)
    private String ciudadResidencia;

    @Column(nullable = false, unique = true)
    private Integer dni;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;

    public Estudiante() {}

    public Estudiante(Integer nroLegajo, String nombres, String apellido, LocalDate fechaNacimiento, String genero, String ciudadResidencia, Integer dni) {
        this.nroLegajo = nroLegajo;
        this.nombres = nombres;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.ciudadResidencia = ciudadResidencia;
        this.dni = dni;
    }

    public Integer getNroLegajo() {
        return nroLegajo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getCiudadResidencia() {
        return ciudadResidencia;
    }

    public void setCiudadResidencia(String ciudadResidencia) {
        this.ciudadResidencia = ciudadResidencia;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}