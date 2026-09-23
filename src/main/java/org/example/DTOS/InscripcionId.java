package org.example.DTOS;

import java.io.Serializable;
import java.util.Objects;

public class InscripcionId implements Serializable {
    private Integer nroLegajo;
    private Integer idCarrera;

    public InscripcionId() {}

    public InscripcionId(Integer nroLegajo, Integer idCarrera) {
        this.nroLegajo = nroLegajo;
        this.idCarrera = idCarrera;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InscripcionId that = (InscripcionId) o;
        return Objects.equals(nroLegajo, that.nroLegajo) && Objects.equals(idCarrera, that.idCarrera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroLegajo, idCarrera);
    }
}
