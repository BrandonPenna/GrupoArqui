package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.dto.EstadoFilaVirtualDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación simplificada de Fila Virtual para eventos de alta demanda (Req. 6 / US-17).
 * Mantiene una cola FIFO ordenada por llegada para cada evento y habilita a comprar por tandas.
 */
@Service
public class FilaVirtualService {

    public static final int TAMANIO_TANDA_DEFAULT = 5;

    private final Map<Long, List<String>> colasPorEvento = new ConcurrentHashMap<>();

    public synchronized EstadoFilaVirtualDTO ingresarAFila(Long eventoId, String clienteId) {
        List<String> cola = colasPorEvento.computeIfAbsent(eventoId, id -> new ArrayList<>());
        if (!cola.contains(clienteId)) {
            cola.add(clienteId);
        }
        return construirEstado(eventoId, clienteId, cola);
    }

    public synchronized EstadoFilaVirtualDTO consultarEstado(Long eventoId, String clienteId) {
        List<String> cola = colasPorEvento.get(eventoId);
        if (cola == null || !cola.contains(clienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El cliente no se encuentra en la fila virtual de este evento");
        }
        return construirEstado(eventoId, clienteId, cola);
    }

    public synchronized boolean estaHabilitado(Long eventoId, String clienteId) {
        List<String> cola = colasPorEvento.get(eventoId);
        if (cola == null) {
            return false;
        }
        int idx = cola.indexOf(clienteId);
        return idx >= 0 && (idx + 1) <= TAMANIO_TANDA_DEFAULT;
    }

    public synchronized void consumirTurno(Long eventoId, String clienteId) {
        List<String> cola = colasPorEvento.get(eventoId);
        if (cola != null) {
            cola.remove(clienteId);
        }
    }

    public synchronized int avanzarTanda(Long eventoId) {
        List<String> cola = colasPorEvento.get(eventoId);
        if (cola == null || cola.isEmpty()) {
            return 0;
        }
        int aRemover = Math.min(TAMANIO_TANDA_DEFAULT, cola.size());
        cola.subList(0, aRemover).clear();
        return cola.size();
    }

    public synchronized void limpiarTodo() {
        colasPorEvento.clear();
    }

    private EstadoFilaVirtualDTO construirEstado(Long eventoId, String clienteId, List<String> cola) {
        int posicion = cola.indexOf(clienteId) + 1;
        boolean habilitado = posicion > 0 && posicion <= TAMANIO_TANDA_DEFAULT;
        String msg = habilitado
                ? "Es tu turno. Estás en la tanda actual habilitada para reservar."
                : "Estás en la posición " + posicion + " de la fila virtual. Aguardá tu tanda.";

        return EstadoFilaVirtualDTO.builder()
                .eventoId(eventoId)
                .clienteId(clienteId)
                .posicion(posicion)
                .totalEnFila(cola.size())
                .tamanioTanda(TAMANIO_TANDA_DEFAULT)
                .habilitadoParaComprar(habilitado)
                .mensaje(msg)
                .build();
    }
}
