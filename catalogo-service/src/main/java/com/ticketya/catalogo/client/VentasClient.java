package com.ticketya.catalogo.client;

import com.ticketya.catalogo.config.TrazabilidadHttpFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
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

    // Obtiene IDs de eventos co-comprados por otros clientes con mismo historial ("quienes compraron para... también compraron para...")
    public List<Long> obtenerEventosCoComprados(String clienteId) {
        try {
            List<Long> ids = restClient.get()
                    .uri("/internal/recomendaciones/co-compras/{clienteId}", clienteId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Long>>() {});
            return ids != null ? ids : List.of();
        } catch (Exception e) {
            log.debug("Fallback recomendaciones historial: ventas-service no disponible ({})", e.getMessage());
            return List.of();
        }
    }
}
