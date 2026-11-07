package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.model.*;
import com.ticketya.ventas_service.repository.CompraRepository;
import com.ticketya.ventas_service.repository.EntradaRepository;
import com.ticketya.ventas_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentasServices {

    private static final int MINUTOS_EXPIRACION_RESERVA = 10;
    private static final int MAX_ENTRADAS_POR_CLIENTE_EVENTO = 6;
    private static final List<EstadoEntrada> ESTADOS_OCUPADOS = List.of(
            EstadoEntrada.RESERVADA,
            EstadoEntrada.VENDIDA
    );
    private final ReservaRepository reservaRepository;
    private final EntradaRepository entradaRepository;
    private final CompraRepository compraRepository;
    private final PasarelaPagosMockService pasarelaPagosMockService;

    // 1. Crear reserva temporal por 10 minutos con bloqueo pesimista (Evita doble venta)
    @Transactional
    public Reserva crearReservaTemporal(ReservaCreateDTO dto, String clienteId) {

        if (dto.getAsientos() == null || dto.getAsientos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos un lugar");
        }
        // Liberar preventivamente reservas ya vencidas antes de validar
        liberarReservasVencidas();
        // Validar límite de entradas por cliente para un mismo evento (Req. 4)
        long actualesCliente = entradaRepository.countEntradasActivasClienteEnEvento(
                dto.getEventoId(), clienteId, ESTADOS_OCUPADOS);
        if (actualesCliente + dto.getAsientos().size() > MAX_ENTRADAS_POR_CLIENTE_EVENTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Supera el límite máximo de " + MAX_ENTRADAS_POR_CLIENTE_EVENTO + " entradas por cliente para este evento");
        }
        // Si el sector NO es numerado, bloqueamos y validamos cupo máximo del sector
        if (Boolean.FALSE.equals(dto.getEsNumerado())) {
            List<Entrada> ocupadasSector = entradaRepository.findEntradasSectorConLock(
                    dto.getEventoId(), dto.getSectorId(), ESTADOS_OCUPADOS);
            if (dto.getCapacidadMaximaSector() != null &&
                    ocupadasSector.size() + dto.getAsientos().size() > dto.getCapacidadMaximaSector()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay suficiente capacidad disponible en el sector");
            }
        }
        LocalDateTime ahora = LocalDateTime.now();

        Reserva reserva = Reserva.builder()
                .clienteId(clienteId)
                .eventoId(dto.getEventoId())
                .fechaCreacion(ahora)
                .fechaExpiracion(ahora.plusMinutes(MINUTOS_EXPIRACION_RESERVA))
                .estado(EstadoReserva.PENDIENTE)
                .build();

        for (AsientoPedidoDTO pedido : dto.getAsientos()) {
            // Si el sector es numerado, aplicamos bloqueo pesimista por fila y asiento
            if (!Boolean.FALSE.equals(dto.getEsNumerado())) {
                List<Entrada> ocupados = entradaRepository.findAsientoOcupadoConLock(
                        dto.getEventoId(), dto.getSectorId(), pedido.getFila(), pedido.getAsiento(), ESTADOS_OCUPADOS);
                if (!ocupados.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El asiento fila " + pedido.getFila() + " número " + pedido.getAsiento() + " ya se encuentra ocupado");
                }
            }

            Entrada entrada = Entrada.builder()
                    .eventoId(dto.getEventoId())
                    .sectorId(dto.getSectorId())
                    .fila(pedido.getFila())
                    .asiento(pedido.getAsiento())
                    .precio(pedido.getPrecio() != null ? pedido.getPrecio() : BigDecimal.ZERO)
                    .estado(EstadoEntrada.RESERVADA)
                    .reserva(reserva)
                    .build();
            reserva.getEntradas().add(entrada);

        }

        return reservaRepository.save(reserva);
    }

    // 2. Confirmar compra, procesar pago mock y emitir entradas con QR único
    @Transactional
    public Compra confirmarCompra(ConfirmarCompraDTO dto, String clienteId) {

        Reserva reserva = reservaRepository.findById(dto.getReservaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (!reserva.getClienteId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La reserva pertenece a otro cliente");
        }

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya fue procesada o cancelada");
        }

        // Verificar si expiraron los 10 minutos
        if (LocalDateTime.now().isAfter(reserva.getFechaExpiracion())) {
            marcarReservaComoExpirada(reserva);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ha expirado (superó los 10 minutos)");
        }

        BigDecimal montoTotal = reserva.getEntradas().stream()
                .map(Entrada::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Llamar a la pasarela de pagos simulada (Mock)
        String transaccionId = pasarelaPagosMockService.procesarPago(clienteId, montoTotal, dto.getMedioPago());
        // Si el pago es rechazado, se liberan los lugares y se devuelve 402 Payment Required
        if (transaccionId == null) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reserva.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.CANCELADA));
            reservaRepository.save(reserva);
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado por la pasarela. Se liberaron los asientos.");
        }
        // Pago aprobado: crear compra y emitir QR único para cada entrada
        Compra compra = Compra.builder()
                .clienteId(clienteId)
                .eventoId(reserva.getEventoId())
                .montoTotal(montoTotal)
                .fechaCompra(LocalDateTime.now())
                .medioPago(dto.getMedioPago())
                .transaccionExternaId(transaccionId)
                .build();
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        for (Entrada entrada : reserva.getEntradas()) {
            entrada.setEstado(EstadoEntrada.VENDIDA);
            entrada.setCodigoQR("QR-" + UUID.randomUUID());
            entrada.setCompra(compra);
            compra.getEntradas().add(entrada);
        }
        reservaRepository.save(reserva);
        return compraRepository.save(compra);
    }
    // 3. Tarea programada cada 1 minuto para liberar reservas vencidas (Flujo B del diagrama de secuencia)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarReservasVencidas() {
        List<Reserva> vencidas = reservaRepository.findByEstadoAndFechaExpiracionBefore(
                EstadoReserva.PENDIENTE, LocalDateTime.now());
        for (Reserva reserva : vencidas) {
            marcarReservaComoExpirada(reserva);
        }
    }
    private void marcarReservaComoExpirada(Reserva reserva) {
        reserva.setEstado(EstadoReserva.EXPIRADA);
        reserva.getEntradas().forEach(e -> e.setEstado(EstadoEntrada.CANCELADA));
        reservaRepository.save(reserva);
    }
    public List<Reserva> listarReservasCliente(String clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }
    public List<Compra> listarComprasCliente(String clienteId) {
        return compraRepository.findByClienteId(clienteId);
    }

}
