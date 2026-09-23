package org.example.DTOS;
import lombok.*;

@Getter @Setter 
@AllArgsConstructor 
public class EstudianteDTO {
    private Integer nroLegajo;
    private String nombres;
    private String apellido;
    private Integer dni;


    public String toString() {
        return "EstudianteDTO{" +
                "nroLegajo=" + nroLegajo +
                ", nombres='" + nombres + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni=" + dni +
                '}';
    }
}
