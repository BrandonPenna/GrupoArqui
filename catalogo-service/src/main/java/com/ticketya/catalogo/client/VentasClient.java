package com.ticketya.catalogo.client;

import com.ticketya.catalogo.config.TrazabilidadHttpFilter;
import org.slf4j.MDC;
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
                .requestInterceptor((request, body, execution) -> {
                    String correlationId = MDC.get(TrazabilidadHttpFilter.CORRELATION_ID_MDC_KEY);
                    if (correlationId != null && !correlationId.isBlank()) {
                        request.getHeaders().set(TrazabilidadHttpFilter.CORRELATION_ID_HEADER, correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

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
