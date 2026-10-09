package org.example.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "carrera")
public class Carrera implements Serializable {
    @Id
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "carrera", nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer duracion;

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;

    public Carrera() {}
    public Carrera(String nombre, Integer duracion) { this.nombre = nombre; this.duracion = duracion; }
    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }
    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public void setInscripciones(List<Inscripcion> inscripciones) { this.inscripciones = inscripciones; }
}
