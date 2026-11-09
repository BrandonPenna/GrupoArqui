package com.ticketya.catalogo.service;

import com.ticketya.catalogo.dto.LugarDTO;
import com.ticketya.catalogo.model.Lugar;
import com.ticketya.catalogo.model.Sector;
import com.ticketya.catalogo.repository.LugarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LugarService {

    private final LugarRepository lugarRepository;

    @Transactional
    public Lugar crearLugar(LugarDTO dto) {
        Lugar lugar = Lugar.builder()
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .ciudad(dto.getCiudad())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .build();

        if (dto.getSectores() != null) {
            for (var secDto : dto.getSectores()) {
                Sector sector = Sector.builder()
                        .nombre(secDto.getNombre())
                        .capacidad(secDto.getCapacidad())
                        .esNumerado(secDto.getEsNumerado())
                        .filas(secDto.getFilas())
                        .asientosPorFila(secDto.getAsientosPorFila())
                        .lugar(lugar)
                        .build();
                lugar.getSectores().add(sector);
            }
        }
        return lugarRepository.save(lugar);
    }

    public List<Lugar> listarTodos() {
        return lugarRepository.findAll();
    }

    public Lugar buscarPorId(Long id) {
        return lugarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado con ID: " + id));
    }
}