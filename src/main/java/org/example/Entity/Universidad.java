package org.example.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "universidad")
public class Universidad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_universidad")
    private Integer idUniversidad;

    @Column(nullable = false)
    private String nombre;

    // Relación bidireccional con Carrera (1 a N)
    @OneToMany(mappedBy = "universidad", cascade = CascadeType.ALL)
    private List<Carrera> carreras;

    public Universidad() {}

    public Universidad(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdUniversidad() {
        return idUniversidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }
}

