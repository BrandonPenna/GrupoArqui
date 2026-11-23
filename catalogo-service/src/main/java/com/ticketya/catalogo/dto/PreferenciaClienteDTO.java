package com.ticketya.catalogo.dto;

import com.ticketya.catalogo.model.TipoEvento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PreferenciaClienteDTO {

    @Schema(description = "Tipos de evento de interés del cliente", example = "[\"RECITAL\", \"FESTIVAL\"]")
    private List<TipoEvento> tiposEvento = new ArrayList<>();

    @Schema(description = "Artistas, bandas o participantes favoritos", example = "[\"Los Piojos\", \"Divididos\"]")
    private List<String> artistasFavoritos = new ArrayList<>();

    @Schema(description = "Ciudad preferida del cliente", example = "Tandil")
    private String ciudad;
}
