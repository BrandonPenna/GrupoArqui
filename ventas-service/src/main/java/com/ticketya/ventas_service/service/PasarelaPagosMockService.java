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
}