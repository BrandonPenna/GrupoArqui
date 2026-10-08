package org.example.Repository;

import java.util.List;

import org.example.Entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    List<Estudiante> findAllByGeneroIgnoreCaseOrderByNroLegajoAsc(String genero);

    boolean existsByDni(Integer dni);
}
