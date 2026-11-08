package com.ticketya.ventas_service.repository;

import com.ticketya.ventas_service.model.CupoSector;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CupoSectorRepository extends JpaRepository<CupoSector, Long> {

    // La fila existe, entonces el bloqueo pesimista sí serializa a los compradores concurrentes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CupoSector c WHERE c.eventoId = :eventoId AND c.sectorId = :sectorId")
    Optional<CupoSector> findForUpdate(@Param("eventoId") Long eventoId, @Param("sectorId") Long sectorId);

    boolean existsByEventoIdAndSectorId(Long eventoId, Long sectorId);
}
