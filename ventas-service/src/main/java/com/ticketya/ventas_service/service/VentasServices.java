package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.CancelacionEventoResultadoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.dto.SolicitudDevolucionDTO;
import com.ticketya.ventas_service.model.*;
import com.ticketya.ventas_service.repository.CompraRepository;
import com.ticketya.ventas_service.repository.DevolucionRepository;
import com.ticketya.ventas_service.repository.EntradaRepository;
import com.ticketya.ventas_service.repository.ReservaRepository;
import com.ticketya.ventas_service.repository.CupoSectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
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
    private final CatalogoClient catalogoClient;
    private final DevolucionRepository devolucionRepository;
    private final CupoSectorRepository cupoSectorRepository;
    private final CupoSectorInicializador cupoSectorInicializador;

    // 1. Crear reserva temporal por 10 minutos.
    // Evita la doble venta con reglas en la base de datos (funcionan aunque haya varias instancias):
    //  - Numerado: UNIQUE sobre entradas.clave_asiento ("evento-sector-fila-asiento").
    //  - No numerado: SELECT ... FOR UPDATE sobre la fila de cupo del sector.
    @Transactional
    public Reserva crearReservaTemporal(ReservaCreateDTO dto, String clienteId) {

        if (dto.getAsientos() == null || dto.getAsientos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos un lugar");
        }

        EventoCatalogoDTO evento = catalogoClient.obtenerEvento(dto.getEventoId());
        // Solo se puede reservar si el evento está publicado (no suspendido ni cancelado)
        if (evento.getEstado() != null && !"PUBLICADO".equals(evento.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento no está disponible para la venta");
        }

                // Sector, tipo, capacidad y precio salen de catálogo, no del request
        EventoCatalogoDTO.PrecioSectorCatalogoDTO precioSector = evento.getPreciosSectores().stream()
                .filter(ps -> ps.getSector() != null && ps.getSector().getId() != null
                        && ps.getSector().getId().equals(dto.getSectorId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El sector no pertenece al evento"));
        EventoCatalogoDTO.SectorCatalogoDTO sector = precioSector.getSector();
        boolean esNumerado = Boolean.TRUE.equals(sector.getEsNumerado());
        BigDecimal precio = precioSector.getPrecio();

        if (esNumerado) {
            for (AsientoPedidoDTO p : dto.getAsientos()) {
                if (p.getFila() == null || p.getAsiento() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Los sectores numerados requieren fila y asiento");
                }
                if ((sector.getFilas() != null && (p.getFila() < 1 || p.getFila() > sector.getFilas()))
                        || (sector.getAsientosPorFila() != null
                            && (p.getAsiento() < 1 || p.getAsiento() > sector.getAsientosPorFila()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Fila o asiento fuera del rango del sector");
                }
            }
        }
        
        // Validar límite de entradas por cliente para un mismo evento (Req. 4)
        long actualesCliente = entradaRepository.countEntradasActivasClienteEnEvento(
                dto.getEventoId(), clienteId, ESTADOS_OCUPADOS);
        if (actualesCliente + dto.getAsientos().size() > MAX_ENTRADAS_POR_CLIENTE_EVENTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Supera el límite máximo de " + MAX_ENTRADAS_POR_CLIENTE_EVENTO + " entradas por cliente para este evento");
        }
        // Si el sector NO es numerado, bloqueamos la fila de cupo del sector y validamos la capacidad.
        // Como la fila existe, el FOR UPDATE sí serializa a los compradores concurrentes.
               if (!esNumerado) {
            cupoSectorInicializador.crearSiNoExiste(dto.getEventoId(), dto.getSectorId(), sector.getCapacidad());
            CupoSector cupo = cupoSectorRepository.findForUpdate(dto.getEventoId(), dto.getSectorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se encontró el cupo del sector"));
            if (cupo.disponibles() < dto.getAsientos().size()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay suficiente capacidad disponible en el sector");
            }
            cupo.setOcupados(cupo.getOcupados() + dto.getAsientos().size());
            cupoSectorRepository.save(cupo);
        }
        LocalDateTime ahora = LocalDateTime.now();

        Reserva reserva = Reserva.builder()
                .clienteId(clienteId)
                .eventoId(dto.getEventoId())
                .fechaCreacion(ahora)
                .fechaExpiracion(ahora.plusMinutes(MINUTOS_EXPIRACION_RESERVA))
                .estado(EstadoReserva.PENDIENTE)
                .fechaEvento(evento.getFechaHora())
                .build();

        for (AsientoPedidoDTO pedido : dto.getAsientos()) {
            // Si el sector es numerado, la entrada lleva la clave única del asiento.
            // Si otro cliente ya lo tiene, la base rechaza el INSERT (409, ver GlobalExceptionHandler).
            String clave = null;
            if (!Boolean.FALSE.equals(dto.getEsNumerado())) {
                clave = dto.getEventoId() + "-" + dto.getSectorId() + "-" + pedido.getFila() + "-" + pedido.getAsiento();
            }

            Entrada entrada = Entrada.builder()
                    .eventoId(dto.getEventoId())
                    .sectorId(dto.getSectorId())
                    .fila(pedido.getFila())
                    .asiento(pedido.getAsiento())
                    .claveAsiento(clave)
                    .precio(pedido.getPrecio() != null ? pedido.getPrecio() : BigDecimal.ZERO)
                    .estado(EstadoEntrada.RESERVADA)
                    .reserva(reserva)
                    .build();
            reserva.getEntradas().add(entrada);

        }

        // saveAndFlush: si la clave de algún asiento ya existe, el error salta acá mismo
        return reservaRepository.saveAndFlush(reserva);
    }

    // 2. Confirmar compra, procesar pago mock y emitir entradas con QR único
    // noRollbackFor: si el pago se rechaza o la reserva venció, la liberación de lugares se guarda aunque se responda error
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Compra confirmarCompra(ConfirmarCompraDTO dto, String clienteId) {

        // Bloqueo de la reserva: dos "Pagar" simultáneos de la misma reserva se atienden de a uno
        Reserva reserva = reservaRepository.findByIdForUpdate(dto.getReservaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (!reserva.getClienteId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La reserva pertenece a otro cliente");
        }

        // Reintento de un pago que ya se aprobó (ej. se cortó la conexión): se devuelve la misma compra
        // y NO se vuelve a llamar a la pasarela, así el cliente no paga dos veces.
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            return compraRepository.findByReservaId(reserva.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya fue procesada"));
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
            reserva.getEntradas().forEach(e -> liberarLugar(e, EstadoEntrada.CANCELADA));
            reservaRepository.save(reserva);
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado por la pasarela. Se liberaron los asientos.");
        }
        // Pago aprobado: crear compra y emitir QR único para cada entrada
        Compra compra = Compra.builder()
                .reservaId(reserva.getId())
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
        reserva.getEntradas().forEach(e -> liberarLugar(e, EstadoEntrada.CANCELADA));
        reservaRepository.save(reserva);
    }

    // Libera el lugar de una entrada: en numerados borra la clave del asiento (vuelve a estar disponible),
    // en no numerados devuelve el lugar al cupo del sector.
    private void liberarLugar(Entrada entrada, EstadoEntrada nuevoEstado) {
        boolean ocupabaLugar = ESTADOS_OCUPADOS.contains(entrada.getEstado());
        entrada.setEstado(nuevoEstado);
        entrada.setClaveAsiento(null);
        if (ocupabaLugar && entrada.getFila() == null) {
            cupoSectorRepository.findForUpdate(entrada.getEventoId(), entrada.getSectorId())
                    .ifPresent(cupo -> {
                        cupo.setOcupados(Math.max(0, cupo.getOcupados() - 1));
                        cupoSectorRepository.save(cupo);
                    });
        }
    }
    public List<Reserva> listarReservasCliente(String clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }
    public List<Compra> listarComprasCliente(String clienteId) {
        return compraRepository.findByClienteId(clienteId);
    }

    //4. Solicitar devolución de una entrada vendida con al menos 72 horas de anticipación
    @Transactional 
    public Devolucion solicitarDevolucion(Long entradaId, String clienteId, SolicitudDevolucionDTO dto){

        Entrada entrada = entradaRepository.findById(entradaId).
                            orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if(entrada.getReserva() == null || !entrada.getReserva().getClienteId().equals(clienteId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La entrada no pertenece al cliente");
        }

        if(entrada.getEstado() != EstadoEntrada.VENDIDA){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden devolver entradas vendidas");
        }

        LocalDateTime fechaEvento = entrada.getReserva().getFechaEvento();
        if(fechaEvento == null){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener la fecha del evento");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if(fechaEvento.isBefore(ahora.plusHours(72))){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede devolver la entrada a menos de 72 horas del evento");
        }
        
        boolean yaTieneSolicitudActiva = devolucionRepository.existsByEntrada_IdAndEstadoIn(
            entradaId,
            List.of(EstadoDevolucion.SOLICITADA, EstadoDevolucion.APROBADA)
        );

        if(yaTieneSolicitudActiva){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe una solicitud de devolución activa para esta entrada"
            );
        }

        Devolucion devolucion = Devolucion.builder()
                .entrada(entrada)
                .clienteId(clienteId)
                .motivo(dto.getMotivo())
                .fechaSolicitud(ahora)
                .estado(EstadoDevolucion.SOLICITADA)
                .build();

        return devolucionRepository.save(devolucion);
    }

    @Transactional
    public Devolucion resolverDevolucion(Long devolucionId, boolean aprobar){
        Devolucion devolucion = devolucionRepository.findByIdForUpdate(devolucionId).
            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada"));
    
        if(devolucion.getEstado() != EstadoDevolucion.SOLICITADA){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La devolución ya fue procesada");
        }

        if(aprobar){
            Entrada entrada = devolucion.getEntrada();
            Compra compra = entrada.getCompra();

            if(compra == null){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener la compra asociada a la entrada");
            }

            String reintegroId = pasarelaPagosMockService.procesarReintegro(
                compra.getTransaccionExternaId(),
                entrada.getPrecio()
            );

            devolucion.setTransaccionReintegroId(reintegroId);
            liberarLugar(entrada, EstadoEntrada.DEVUELTA);
            devolucion.setEstado(EstadoDevolucion.APROBADA);
        }else{
            devolucion.setEstado(EstadoDevolucion.RECHAZADA);
        }

        devolucion.setFechaResolucion(LocalDateTime.now());
        return devolucionRepository.save(devolucion);
    }

    //5. Cancelar evento: reintegra las entradas vendidas y cancela las reservas pendientes
    @Transactional
    public CancelacionEventoResultadoDTO cancelarEvento(Long eventoId) {

        List<Reserva> pendientes = reservaRepository.findByEventoIdAndEstado(eventoId, EstadoReserva.PENDIENTE);
        for (Reserva reserva : pendientes) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reserva.getEntradas().forEach(e -> liberarLugar(e, EstadoEntrada.CANCELADA));
            reservaRepository.save(reserva);
        }

        Map<Long, Devolucion> solicitadasPorEntrada = new HashMap<>();
        for (Devolucion d : devolucionRepository.findByEntrada_EventoIdAndEstado(eventoId, EstadoDevolucion.SOLICITADA)) {
            solicitadasPorEntrada.put(d.getEntrada().getId(), d);
        }

        Set<String> compradores = new HashSet<>();
        List<Entrada> vendidas = entradaRepository.findByEventoIdAndEstado(eventoId, EstadoEntrada.VENDIDA);
        for (Entrada entrada : vendidas) {
            String clienteId = entrada.getReserva().getClienteId();
            String reintegroId = pasarelaPagosMockService.procesarReintegro(
                entrada.getCompra().getTransaccionExternaId(),
                entrada.getPrecio()
            );

            Devolucion devolucion = solicitadasPorEntrada.getOrDefault(entrada.getId(), Devolucion.builder()
                    .entrada(entrada)
                    .clienteId(clienteId)
                    .fechaSolicitud(LocalDateTime.now())
                    .build());
            devolucion.setMotivo("Cancelación del evento");
            devolucion.setEstado(EstadoDevolucion.APROBADA);
            devolucion.setFechaResolucion(LocalDateTime.now());
            devolucion.setTransaccionReintegroId(reintegroId);
            devolucionRepository.save(devolucion);

            liberarLugar(entrada, EstadoEntrada.REINTEGRADA_POR_CANCELACION);
            entradaRepository.save(entrada);
            compradores.add(clienteId);
        }

        for (String clienteId : compradores) {
            log.info("[NOTIFICACION] a={} : El evento {} fue cancelado. Se inició el reintegro de tus entradas.",
                    clienteId, eventoId);
        }

        return new CancelacionEventoResultadoDTO(eventoId, pendientes.size(), vendidas.size(), compradores.size());
    }
}
