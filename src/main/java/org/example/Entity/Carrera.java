package org.example.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "carrera")
public class Carrera implements Serializable {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer duracion;

    // Relación N a 1 con Universidad
    @ManyToOne
    @JoinColumn(name = "id_universidad", nullable = false)
    private Universidad universidad;

    // Relación bidireccional con la tabla intermedia Inscripcion
    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;

    public Carrera() {}

    public Carrera(String nombre, Universidad universidad, Integer duracion) {
        this.nombre = nombre;
        this.universidad = universidad;
        this.duracion = duracion;
    }

    public Integer getIdCarrera() {
        return idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public Universidad getUniversidad() {
        return universidad;
    }

    public void setUniversidad(Universidad universidad) {
        this.universidad = universidad;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera=idCarrera;
    }
}
