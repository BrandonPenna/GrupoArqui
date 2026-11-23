package com.ticketya.ventas_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class PasarelaPagosMockService {

    private static final int MAX_REINTENTOS = 3;

    public String procesarPago(String clienteId, BigDecimal montoTotal, String medioPago) {
        if ("RECHAZAR".equalsIgnoreCase(medioPago)) {
            return null;
        }

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                if ("FALLO_TRANSITORIO".equalsIgnoreCase(medioPago) && intento == 1) {
                    throw new RuntimeException("Timeout transitorio conectando con pasarela de pagos");
                }
                if ("TIMEOUT".equalsIgnoreCase(medioPago) || "CAIDO".equalsIgnoreCase(medioPago)) {
                    throw new RuntimeException("Pasarela de pagos externa no disponible (Timeout)");
                }
                return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } catch (RuntimeException ex) {
                log.warn("[RESILIENCIA_PAGOS] Reintento {}/{} para cliente={} medioPago={} causa={}",
                        intento, MAX_REINTENTOS, clienteId, medioPago, ex.getMessage());
            }
        }

        log.error("[FALLBACK_PAGOS] Se agotaron los {} reintentos con la pasarela para cliente={}. Se activa fallback.",
                MAX_REINTENTOS, clienteId);
        return null;
    }

    public String procesarReintegro(String transaccionExternaId, BigDecimal monto) {
        if (transaccionExternaId == null || transaccionExternaId.isBlank()) {
            throw new IllegalArgumentException("La transacción original es obligatoria");
        }
        if (monto == null || monto.signum() < 0) {
            throw new IllegalArgumentException("El monto del reintegro no puede ser negativo");
        }

        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
