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

    // Relaciones de objeto (opcionales pero útiles para navegar desde Java)
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
    public Integer getNroLegajo() { return nroLegajo; }
    public void setNroLegajo(Integer nroLegajo) { this.nroLegajo = nroLegajo; }

    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }

    public Boolean getGraduado() { return graduado; }
    public void setGraduado(Boolean graduado) { this.graduado = graduado; }
}