package org.example.Repository;


import org.example.Entity.InscripcionId;
import org.example.Entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, InscripcionId> {

}