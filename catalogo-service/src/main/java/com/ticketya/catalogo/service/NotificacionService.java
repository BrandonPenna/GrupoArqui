package com.ticketya.catalogo.service;

import com.ticketya.catalogo.dto.NotificacionDTO;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.PreferenciaClienteDocument;
import com.ticketya.catalogo.repository.PreferenciaClienteNoSqlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de notificaciones asíncronas / fire-and-forget con resiliencia (Req. 11 / US-21).
 * Ante cualquier fallo del proveedor externo de notificaciones, aplica reintentos y fallback en bitácora
 * para que la operación principal (publicación, compra o cancelación) nunca se vea afectada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final int MAX_REINTENTOS = 2;

    private final PreferenciaClienteNoSqlRepository preferenciaRepository;
    private final Map<String, List<NotificacionDTO>> bandejaPorCliente = new ConcurrentHashMap<>();

    public void notificarPublicacionArtistaFavorito(Evento evento) {
        try {
            if (evento == null || evento.getLugar() == null || evento.getParticipantes() == null) {
                return;
            }
            String ciudadEvento = evento.getLugar().getCiudad();
            for (PreferenciaClienteDocument pref : preferenciaRepository.findAll()) {
                boolean mismaCiudad = pref.getCiudad() != null
                        && ciudadEvento != null
                        && pref.getCiudad().equalsIgnoreCase(ciudadEvento);
                if (!mismaCiudad || pref.getArtistasFavoritos() == null) {
                    continue;
                }
                for (String artistaFav : pref.getArtistasFavoritos()) {
                    boolean participa = evento.getParticipantes().stream()
                            .anyMatch(p -> p.equalsIgnoreCase(artistaFav)
                                    || p.toLowerCase().contains(artistaFav.toLowerCase()));
                    if (participa) {
                        enviarConResiliencia(
                                pref.getClienteId(),
                                "PUSH",
                                "¡" + artistaFav + " toca en " + ciudadEvento + "!",
                                "Se publicó el evento '" + evento.getNombre() + "' en " + ciudadEvento + "."
                        );
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Fallback resiliente: nunca interrumpe la publicación del evento (US-21)
            log.warn("[FALLBACK_NOTIFICACION] No se pudo enviar alerta de artista favorito para evento {}: {}",
                    evento != null ? evento.getId() : null, e.getMessage());
        }
    }

    public void enviarConResiliencia(String clienteId, String canal, String asunto, String mensaje) {
        int intento = 0;
        while (intento < MAX_REINTENTOS) {
            intento++;
            try {
                NotificacionDTO notif = NotificacionDTO.builder()
                        .destinatarioId(clienteId)
                        .canal(canal)
                        .asunto(asunto)
                        .mensaje(mensaje)
                        .fechaEnvio(LocalDateTime.now())
                        .build();
                bandejaPorCliente
                        .computeIfAbsent(clienteId, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(notif);
                log.info("[NOTIFICACION] canal={} a={} asunto='{}' mensaje='{}'",
                        canal, clienteId, asunto, mensaje);
                return;
            } catch (Exception e) {
                log.warn("[REINTENTO_NOTIFICACION] intento={}/{} cliente={} error={}",
                        intento, MAX_REINTENTOS, clienteId, e.getMessage());
            }
        }
        log.error("[FALLBACK_NOTIFICACION] Servicio externo no disponible tras {} intentos para cliente {}",
                MAX_REINTENTOS, clienteId);
    }

    public List<NotificacionDTO> listarNotificacionesCliente(String clienteId) {
        return bandejaPorCliente.getOrDefault(clienteId, List.of());
    }

    public void limpiarBandeja() {
        bandejaPorCliente.clear();
    }
}
