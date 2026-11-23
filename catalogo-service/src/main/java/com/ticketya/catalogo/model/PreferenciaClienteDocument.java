package com.ticketya.catalogo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo documental NoSQL (colección: preferencias_clientes) para almacenar
 * las preferencias flexibles de cada cliente (Req. 10 / US-19).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaClienteDocument {

    private String id;
    private String clienteId;

    @Builder.Default
    private List<TipoEvento> tiposEvento = new ArrayList<>();

    @Builder.Default
    private List<String> artistasFavoritos = new ArrayList<>();

    private String ciudad;

    private LocalDateTime fechaActualizacion;
}
