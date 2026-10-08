package org.example.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.example.Entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    List<Estudiante> findAllByGeneroIgnoreCaseOrderByNroLegajoAsc(String genero);
    boolean existsByDni(Integer dni);


    // 2g) Estudiantes de una carrera, filtrados por ciudad de residencia
    @Query("SELECT e FROM Estudiante e JOIN e.inscripciones i " +
            "WHERE i.idCarrera = :idCarrera AND LOWER(e.ciudadResidencia) = LOWER(:ciudad) " +
            "ORDER BY e.nroLegajo ASC")
    List<Estudiante> obtenerPorCarreraYCiudad(@Param("idCarrera") Integer idCarrera,
                                              @Param("ciudad") String ciudad);

}