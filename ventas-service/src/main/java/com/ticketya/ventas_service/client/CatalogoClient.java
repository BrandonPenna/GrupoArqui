package com.ticketya.ventas_service.client;

import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(
        @Value("${ticketya.catalogo.url}") String catalogoUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogoUrl)
                .build();
    }

    public EventoCatalogoDTO obtenerEvento(Long eventoId) {
        EventoCatalogoDTO evento = restClient.get()
                .uri("/api/eventos/{id}", eventoId)
                .retrieve()
                .body(EventoCatalogoDTO.class);

        if (evento == null || evento.getFechaHora() == null) {
            throw new IllegalStateException(
                    "Catálogo no devolvió la fecha del evento " + eventoId);
        }

        return evento;
    }
}