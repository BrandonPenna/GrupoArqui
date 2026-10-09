package org.example.Repository;


import org.example.Entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    boolean existsByIdEstudianteAndIdCarrera(Integer idEstudiante, Integer idCarrera);

}
