package com.ticketya.catalogo.repository;

import com.ticketya.catalogo.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    List<Lugar> findByCiudadIgnoreCase(String ciudad);
}