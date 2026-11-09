package com.ticketya.catalogo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VentasClient {

    private final RestClient restClient;

    public VentasClient(@Value("${ticketya.ventas.url}") String ventasUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(ventasUrl)
                .build();
    }

    // Avisa a ventas que el evento se cancela para que reintegre las entradas
    public void procesarCancelacion(Long eventoId) {
        try {
            restClient.post()
                    .uri("/internal/eventos/{id}/cancelacion", eventoId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo procesar el reintegro de entradas en ventas-service. El evento no fue cancelado.");
        }
    }
}
