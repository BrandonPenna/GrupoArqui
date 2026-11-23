package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.CancelacionEventoResultadoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import com.ticketya.ventas_service.dto.ReporteEventoDTO;
import com.ticketya.ventas_service.dto.ReporteSectorDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.dto.SolicitudDevolucionDTO;
import com.ticketya.ventas_service.dto.ValidarQrRequestDTO;
import com.ticketya.ventas_service.dto.ValidarQrResponseDTO;
import com.ticketya.ventas_service.model.*;
import com.ticketya.ventas_service.repository.CompraRepository;
import com.ticketya.ventas_service.repository.CupoSectorRepository;
import com.ticketya.ventas_service.repository.DevolucionRepository;
import com.ticketya.ventas_service.repository.EntradaRepository;
import com.ticketya.ventas_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    @Setter
    @Autowired(required = false)
    private FilaVirtualService filaVirtualService = new FilaVirtualService();

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

        // Si el evento es de alta demanda, validar que el cliente esté habilitado en la tanda actual de la fila virtual (Req. 6 / US-17)
        if (Boolean.TRUE.equals(evento.getAltaDemanda()) && filaVirtualService != null) {
            if (!filaVirtualService.estaHabilitado(dto.getEventoId(), clienteId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Evento de alta demanda: debe ingresar a la fila virtual y esperar su tanda para reservar");
            }
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
            if (esNumerado) {
                clave = dto.getEventoId() + "-" + dto.getSectorId() + "-" + pedido.getFila() + "-" + pedido.getAsiento();
            }

            Entrada entrada = Entrada.builder()
                    .eventoId(dto.getEventoId())
                    .sectorId(dto.getSectorId())
                    .fila(pedido.getFila())
                    .asiento(pedido.getAsiento())
                    .claveAsiento(clave)
                    .precio(precio)
                    .estado(EstadoEntrada.RESERVADA)
                    .reserva(reserva)
                    .build();
            reserva.getEntradas().add(entrada);

        }

        // saveAndFlush: si la clave de algún asiento ya existe, el error salta acá mismo
        Reserva guardada = reservaRepository.saveAndFlush(reserva);

        if (Boolean.TRUE.equals(evento.getAltaDemanda()) && filaVirtualService != null) {
            filaVirtualService.consumirTurno(dto.getEventoId(), clienteId);
        }

        return guardada;
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
        Compra guardada = compraRepository.save(compra);

        log.info("[OPERACION_CRITICA] tipo=COMPRA usuario={} eventoId={} reservaId={} transaccionId={} monto={}",
                clienteId, reserva.getEventoId(), reserva.getId(), transaccionId, montoTotal);
        log.info("[NOTIFICACION] canal=EMAIL a={} : Compra confirmada para evento {}. Se emitieron {} entradas con QR.",
                clienteId, reserva.getEventoId(), compra.getEntradas().size());

        return guardada;
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
            log.info("[OPERACION_CRITICA] tipo=DEVOLUCION_APROBADA devolucionId={} entradaId={} reintegroId={}",
                    devolucionId, entrada.getId(), reintegroId);
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

    // 6. Control de acceso con código QR en el ingreso al evento (Req. 8 / US-18)
    @Transactional
    public ValidarQrResponseDTO validarAccesoQR(ValidarQrRequestDTO dto, String personalId) {
        if (dto.getEventoId() == null || dto.getCodigoQR() == null || dto.getCodigoQR().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el eventoId y el codigoQR");
        }

        Entrada entrada = entradaRepository.findByCodigoQRForUpdate(dto.getCodigoQR().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código QR inexistente"));

        if (!entrada.getEventoId().equals(dto.getEventoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La entrada corresponde a otro evento");
        }

        if (entrada.getEstado() == EstadoEntrada.USADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La entrada ya fue utilizada anteriormente");
        }

        if (entrada.getEstado() != EstadoEntrada.VENDIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La entrada no es válida para ingresar (estado actual: " + entrada.getEstado() + ")");
        }

        entrada.setEstado(EstadoEntrada.USADA);
        entradaRepository.save(entrada);

        LocalDateTime ahora = LocalDateTime.now();
        log.info("[OPERACION_CRITICA] tipo=INGRESO_QR usuario={} eventoId={} entradaId={} qr={}",
                personalId, entrada.getEventoId(), entrada.getId(), entrada.getCodigoQR());

        return ValidarQrResponseDTO.builder()
                .accesoPermitido(true)
                .mensaje("Ingreso válido. Entrada marcada como utilizada.")
                .entradaId(entrada.getId())
                .eventoId(entrada.getEventoId())
                .sectorId(entrada.getSectorId())
                .fila(entrada.getFila())
                .asiento(entrada.getAsiento())
                .estado(entrada.getEstado())
                .fechaIngreso(ahora)
                .build();
    }

    // 7. Reporte de ventas, devoluciones, ocupación y recaudación por sector para organizadores (Req. 12 / US-22)
    @Transactional(readOnly = true)
    public ReporteEventoDTO obtenerReporteEvento(Long eventoId, String solicitanteId, String rol) {
        EventoCatalogoDTO evento = catalogoClient.obtenerEvento(eventoId);

        // Si es ORGANIZADOR, solo puede consultar reportes de sus propios eventos (US-22)
        if (!"ADMIN".equalsIgnoreCase(rol)
                && evento.getOrganizadorId() != null
                && !evento.getOrganizadorId().equals(solicitanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tiene permisos para consultar el reporte de un evento de otro organizador");
        }

        List<Entrada> entradasEvento = entradaRepository.findByEventoId(eventoId);

        // Agrupar sectores configurados en el catálogo para el evento
        Map<Long, EventoCatalogoDTO.SectorCatalogoDTO> infoSectores = new LinkedHashMap<>();
        if (evento.getPreciosSectores() != null) {
            for (EventoCatalogoDTO.PrecioSectorCatalogoDTO ps : evento.getPreciosSectores()) {
                if (ps.getSector() != null && ps.getSector().getId() != null) {
                    infoSectores.put(ps.getSector().getId(), ps.getSector());
                }
            }
        }
        // Si existen entradas de algún sector que no vino en la lista del catálogo, lo incluimos también
        for (Entrada e : entradasEvento) {
            infoSectores.computeIfAbsent(e.getSectorId(), id -> {
                EventoCatalogoDTO.SectorCatalogoDTO s = new EventoCatalogoDTO.SectorCatalogoDTO();
                s.setId(id);
                s.setNombre("Sector " + id);
                s.setCapacidad(cupoSectorRepository.findById(id).map(CupoSector::getCapacidad).orElse(0));
                return s;
            });
        }

        List<ReporteSectorDTO> reportesSectores = new ArrayList<>();
        int capacidadTotal = 0;
        long totalVendidas = 0;
        long totalUtilizadas = 0;
        long totalDevueltas = 0;
        BigDecimal recaudacionTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, EventoCatalogoDTO.SectorCatalogoDTO> entry : infoSectores.entrySet()) {
            Long sectorId = entry.getKey();
            EventoCatalogoDTO.SectorCatalogoDTO sectorInfo = entry.getValue();
            int capacidadSector = sectorInfo.getCapacidad() != null ? sectorInfo.getCapacidad() : 0;

            long vendidasSector = 0;
            long utilizadasSector = 0;
            long devueltasSector = 0;
            BigDecimal recaudacionSector = BigDecimal.ZERO;

            for (Entrada e : entradasEvento) {
                if (!sectorId.equals(e.getSectorId())) {
                    continue;
                }
                if (e.getEstado() == EstadoEntrada.VENDIDA || e.getEstado() == EstadoEntrada.USADA) {
                    vendidasSector++;
                    if (e.getEstado() == EstadoEntrada.USADA) {
                        utilizadasSector++;
                    }
                    if (e.getPrecio() != null) {
                        recaudacionSector = recaudacionSector.add(e.getPrecio());
                    }
                } else if (e.getEstado() == EstadoEntrada.DEVUELTA
                        || e.getEstado() == EstadoEntrada.REINTEGRADA_POR_CANCELACION) {
                    devueltasSector++;
                }
            }

            double ocupacionSector = capacidadSector > 0
                    ? Math.round((vendidasSector * 10000.0) / capacidadSector) / 100.0
                    : 0.0;

            reportesSectores.add(ReporteSectorDTO.builder()
                    .sectorId(sectorId)
                    .nombreSector(sectorInfo.getNombre())
                    .capacidad(capacidadSector)
                    .entradasVendidas(vendidasSector)
                    .entradasUtilizadas(utilizadasSector)
                    .entradasDevueltas(devueltasSector)
                    .porcentajeOcupacion(ocupacionSector)
                    .recaudacion(recaudacionSector)
                    .build());

            capacidadTotal += capacidadSector;
            totalVendidas += vendidasSector;
            totalUtilizadas += utilizadasSector;
            totalDevueltas += devueltasSector;
            recaudacionTotal = recaudacionTotal.add(recaudacionSector);
        }

        double ocupacionTotal = capacidadTotal > 0
                ? Math.round((totalVendidas * 10000.0) / capacidadTotal) / 100.0
                : 0.0;

        return ReporteEventoDTO.builder()
                .eventoId(eventoId)
                .nombreEvento(evento.getNombre())
                .organizadorId(evento.getOrganizadorId())
                .estadoEvento(evento.getEstado())
                .capacidadTotal(capacidadTotal)
                .totalEntradasVendidas(totalVendidas)
                .totalEntradasUtilizadas(totalUtilizadas)
                .totalEntradasDevueltas(totalDevueltas)
                .porcentajeOcupacionTotal(ocupacionTotal)
                .recaudacionTotal(recaudacionTotal)
                .sectores(reportesSectores)
                .build();
    }
}
