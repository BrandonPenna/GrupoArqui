package com.ticketya.ventas_service.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PasarelaPagosMockService {

    /**
     * Simula el cobro en una pasarela externa.
     * Si el medio de pago es "RECHAZAR" (útil para pruebas), simula un pago rechazado devolviendo null.
     * En cualquier otro caso, aprueba el pago y devuelve un ID de transacción externa.
     */
    public String procesarPago(String clienteId, BigDecimal montoTotal, String medioPago) {
        if ("RECHAZAR".equalsIgnoreCase(medioPago)) {
            return null;
        }
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simula el reintegro en una pasarela externa.
     * Devuelve un ID de transacción externa de reintegro.
     */
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