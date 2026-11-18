package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.CancelacionEventoResultadoDTO;
import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.dto.SolicitudDevolucionDTO;
import com.ticketya.ventas_service.dto.ReporteEventoDTO;
import com.ticketya.ventas_service.dto.ReporteSectorDTO;
import com.ticketya.ventas_service.dto.ValidarQrRequestDTO;
import com.ticketya.ventas_service.dto.ValidarQrResponseDTO;
import com.ticketya.ventas_service.model.*;
import com.ticketya.ventas_service.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentasServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private EntradaRepository entradaRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private PasarelaPagosMockService pasarelaPagosMockService;
    @Mock private CatalogoClient catalogoClient;
    @Mock private DevolucionRepository devolucionRepository;
    @Mock private CupoSectorRepository cupoSectorRepository;
    @Mock private CupoSectorInicializador cupoSectorInicializador;

    @InjectMocks
    private VentasServices ventasService;

    // Verifica que, con el pago aprobado y una reserva vigente, la compra se registre,
    // la reserva y la entrada cambien a sus estados correspondientes y se genere un QR.
    @Test
    void compraConPagoAprobadoTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        Entrada entrada = reserva.getEntradas().get(0);

        ConfirmarCompraDTO dto = new ConfirmarCompraDTO();
        dto.setReservaId(1L);
        dto.setMedioPago("TARJETA_CREDITO");

        when(reservaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reserva));
        when(pasarelaPagosMockService.procesarPago(
                eq("cliente-1"), any(BigDecimal.class), eq("TARJETA_CREDITO")))
                .thenReturn("PAY-123");
        when(compraRepository.save(any(Compra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Compra compra = ventasService.confirmarCompra(dto, "cliente-1");

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(EstadoEntrada.VENDIDA, entrada.getEstado());
        assertNotNull(entrada.getCodigoQR());
        assertTrue(entrada.getCodigoQR().startsWith("QR-"));
        assertEquals("PAY-123", compra.getTransaccionExternaId());
        assertSame(compra, entrada.getCompra());
    }

    // Verifica que un pago rechazado produzca un error 402, cancele la reserva y sus entradas,
    // y no registre una compra.
    @Test
    void compraSinPagoAprobadoTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        Entrada entrada = reserva.getEntradas().get(0);

        ConfirmarCompraDTO dto = new ConfirmarCompraDTO();
        dto.setReservaId(1L);
        dto.setMedioPago("RECHAZAR");

        when(reservaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reserva));
        when(pasarelaPagosMockService.procesarPago(
                eq("cliente-1"), any(BigDecimal.class), eq("RECHAZAR")))
                .thenReturn(null);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> ventasService.confirmarCompra(dto, "cliente-1")
        );

        assertEquals(HttpStatus.PAYMENT_REQUIRED, error.getStatusCode());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(EstadoEntrada.CANCELADA, entrada.getEstado());
        verify(compraRepository, never()).save(any(Compra.class));
    }

    // Verifica que un cliente pueda solicitar la devolución de una entrada vendida
    // cuando faltan más de 72 horas para el evento.
    @Test
    void devolucionValidaTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        reserva.setFechaEvento(LocalDateTime.now().plusHours(80));

        Entrada entrada = reserva.getEntradas().get(0);
        entrada.setEstado(EstadoEntrada.VENDIDA);

        SolicitudDevolucionDTO dto = new SolicitudDevolucionDTO();
        dto.setMotivo("No puedo asistir");

        when(entradaRepository.findById(1L)).thenReturn(Optional.of(entrada));
        when(devolucionRepository.existsByEntrada_IdAndEstadoIn(
                eq(1L), anyList())).thenReturn(false);
        when(devolucionRepository.save(any(Devolucion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Devolucion devolucion =
                ventasService.solicitarDevolucion(1L, "cliente-1", dto);

        assertEquals(EstadoDevolucion.SOLICITADA, devolucion.getEstado());
        assertEquals("No puedo asistir", devolucion.getMotivo());
        assertSame(entrada, devolucion.getEntrada());
    }

    // Verifica que la solicitud de devolución sea rechazada cuando faltan menos
    // de 72 horas para el evento y que no se guarde ninguna devolución.
    @Test
    void devolucionInvalidaTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        reserva.setFechaEvento(LocalDateTime.now().plusHours(48));

        Entrada entrada = reserva.getEntradas().get(0);
        entrada.setEstado(EstadoEntrada.VENDIDA);

        when(entradaRepository.findById(1L)).thenReturn(Optional.of(entrada));

        SolicitudDevolucionDTO dto = new SolicitudDevolucionDTO();
        dto.setMotivo("No puedo asistir");

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> ventasService.solicitarDevolucion(1L, "cliente-1", dto)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    // Verifica que, al aprobar una devolución, se genere y guarde el reintegro,
    // la devolución quede aprobada y la entrada pase a estado DEVUELTA.
    @Test
    void cambiosDespuesDeDevolucionTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        Entrada entrada = reserva.getEntradas().get(0);
        entrada.setEstado(EstadoEntrada.VENDIDA);

        Compra compra = Compra.builder()
                .transaccionExternaId("PAY-123")
                .build();
        entrada.setCompra(compra);

        Devolucion devolucion = Devolucion.builder()
                .id(10L)
                .entrada(entrada)
                .clienteId("cliente-1")
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoDevolucion.SOLICITADA)
                .build();

        when(devolucionRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(devolucion));
        when(pasarelaPagosMockService.procesarReintegro(
                "PAY-123", entrada.getPrecio()))
                .thenReturn("REF-456");
        when(devolucionRepository.save(any(Devolucion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Devolucion resultado = ventasService.resolverDevolucion(10L, true);

        assertEquals(EstadoDevolucion.APROBADA, resultado.getEstado());
        assertEquals(EstadoEntrada.DEVUELTA, entrada.getEstado());
        assertEquals("REF-456", resultado.getTransaccionReintegroId());
        assertNotNull(resultado.getFechaResolucion());
    }

    // Reintentar el pago de una reserva ya confirmada devuelve la misma compra sin volver a cobrar.
    @Test
    void reintentoDePagoNoCobraDosVecesTest() {
        Reserva reserva = crearReservaVigente("cliente-1");
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        Compra existente = Compra.builder().id(7L).reservaId(1L).transaccionExternaId("PAY-1").build();

        ConfirmarCompraDTO dto = new ConfirmarCompraDTO();
        dto.setReservaId(1L);
        dto.setMedioPago("TARJETA_CREDITO");

        when(reservaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reserva));
        when(compraRepository.findByReservaId(1L)).thenReturn(Optional.of(existente));

        Compra compra = ventasService.confirmarCompra(dto, "cliente-1");

        assertSame(existente, compra);
        verifyNoInteractions(pasarelaPagosMockService);
    }

    @Test
    void cancelacionDeEventoReintegraEntradasTest() {
        Reserva confirmada = crearReservaVigente("cliente-1");
        confirmada.setEstado(EstadoReserva.CONFIRMADA);
        Entrada vendida = confirmada.getEntradas().get(0);
        vendida.setEstado(EstadoEntrada.VENDIDA);
        vendida.setCompra(Compra.builder().transaccionExternaId("PAY-9").build());

        Reserva pendiente = crearReservaVigente("cliente-2");

        when(reservaRepository.findByEventoIdAndEstado(2L, EstadoReserva.PENDIENTE))
                .thenReturn(List.of(pendiente));
        when(devolucionRepository.findByEntrada_EventoIdAndEstado(2L, EstadoDevolucion.SOLICITADA))
                .thenReturn(List.of());
        when(entradaRepository.findByEventoIdAndEstado(2L, EstadoEntrada.VENDIDA))
                .thenReturn(List.of(vendida));
        when(pasarelaPagosMockService.procesarReintegro("PAY-9", vendida.getPrecio()))
                .thenReturn("REF-1");

        CancelacionEventoResultadoDTO resultado = ventasService.cancelarEvento(2L);

        assertEquals(1, resultado.getReservasCanceladas());
        assertEquals(1, resultado.getEntradasReintegradas());
        assertEquals(1, resultado.getClientesNotificados());
        assertEquals(EstadoEntrada.REINTEGRADA_POR_CANCELACION, vendida.getEstado());
        assertEquals(EstadoReserva.CANCELADA, pendiente.getEstado());
        assertEquals(EstadoEntrada.CANCELADA, pendiente.getEntradas().get(0).getEstado());
        verify(devolucionRepository).save(argThat(d ->
                d.getEstado() == EstadoDevolucion.APROBADA && "REF-1".equals(d.getTransaccionReintegroId())));
    }

    @Test
    void cancelacionApruebaDevolucionYaSolicitadaTest() {
        Reserva confirmada = crearReservaVigente("cliente-1");
        Entrada vendida = confirmada.getEntradas().get(0);
        vendida.setEstado(EstadoEntrada.VENDIDA);
        vendida.setCompra(Compra.builder().transaccionExternaId("PAY-9").build());
        Devolucion solicitada = Devolucion.builder()
                .id(5L).entrada(vendida).clienteId("cliente-1")
                .fechaSolicitud(LocalDateTime.now()).estado(EstadoDevolucion.SOLICITADA).build();

        when(reservaRepository.findByEventoIdAndEstado(2L, EstadoReserva.PENDIENTE)).thenReturn(List.of());
        when(devolucionRepository.findByEntrada_EventoIdAndEstado(2L, EstadoDevolucion.SOLICITADA))
                .thenReturn(List.of(solicitada));
        when(entradaRepository.findByEventoIdAndEstado(2L, EstadoEntrada.VENDIDA)).thenReturn(List.of(vendida));
        when(pasarelaPagosMockService.procesarReintegro(any(), any())).thenReturn("REF-2");

        ventasService.cancelarEvento(2L);

        assertEquals(EstadoDevolucion.APROBADA, solicitada.getEstado());
        assertEquals("REF-2", solicitada.getTransaccionReintegroId());
        verify(devolucionRepository).save(solicitada);
    }

    // Verifica que no se pueda reservar en un evento suspendido
    @Test
    void reservaEnEventoSuspendidoTest() {
        EventoCatalogoDTO evento = new EventoCatalogoDTO();
        evento.setFechaHora(LocalDateTime.now().plusDays(10));
        evento.setEstado("SUSPENDIDO");
        when(catalogoClient.obtenerEvento(2L)).thenReturn(evento);

        ReservaCreateDTO dto = new ReservaCreateDTO();
        dto.setEventoId(2L);
        dto.setSectorId(3L);
        dto.setEsNumerado(true);
        dto.setAsientos(List.of(new AsientoPedidoDTO()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> ventasService.crearReservaTemporal(dto, "cliente-1"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verifyNoInteractions(reservaRepository, entradaRepository);
    }

    // Crea una reserva pendiente, vigente y con una entrada para usar como datos de prueba.
    private Reserva crearReservaVigente(String clienteId) {
        Reserva reserva = Reserva.builder()
                .id(1L)
                .clienteId(clienteId)
                .eventoId(2L)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .fechaEvento(LocalDateTime.now().plusDays(10))
                .estado(EstadoReserva.PENDIENTE)
                .entradas(new ArrayList<>())
                .build();

        Entrada entrada = Entrada.builder()
                .id(1L)
                .eventoId(2L)
                .sectorId(3L)
                .fila(1)
                .asiento(5)
                .precio(new BigDecimal("15000.00"))
                .estado(EstadoEntrada.RESERVADA)
                .reserva(reserva)
                .build();

        reserva.getEntradas().add(entrada);
        return reserva;
    }

    // Verifica que un QR válido del evento sea aceptado y cambie el estado de la entrada a USADA (Req. 8 / US-18)
    @Test
    void validarQrExitosoMarcaEntradaComoUsadaTest() {
        Entrada entrada = Entrada.builder()
                .id(15L)
                .eventoId(2L)
                .sectorId(3L)
                .fila(4)
                .asiento(8)
                .codigoQR("QR-VALIDO-123")
                .estado(EstadoEntrada.VENDIDA)
                .build();

        when(entradaRepository.findByCodigoQRForUpdate("QR-VALIDO-123"))
                .thenReturn(Optional.of(entrada));

        ValidarQrRequestDTO req = new ValidarQrRequestDTO();
        req.setEventoId(2L);
        req.setCodigoQR("QR-VALIDO-123");

        ValidarQrResponseDTO resp = ventasService.validarAccesoQR(req, "acceso-1");

        assertTrue(resp.isAccesoPermitido());
        assertEquals(EstadoEntrada.USADA, entrada.getEstado());
        assertEquals(EstadoEntrada.USADA, resp.getEstado());
        assertEquals(15L, resp.getEntradaId());
        verify(entradaRepository).save(entrada);
    }

    // Verifica que un QR ya utilizado anteriormente sea rechazado con 409 Conflict (no puede ingresar dos veces)
    @Test
    void validarQrYaUtilizadoRechazaCon409Test() {
        Entrada entrada = Entrada.builder()
                .id(15L)
                .eventoId(2L)
                .sectorId(3L)
                .codigoQR("QR-USADO-123")
                .estado(EstadoEntrada.USADA)
                .build();

        when(entradaRepository.findByCodigoQRForUpdate("QR-USADO-123"))
                .thenReturn(Optional.of(entrada));

        ValidarQrRequestDTO req = new ValidarQrRequestDTO();
        req.setEventoId(2L);
        req.setCodigoQR("QR-USADO-123");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> ventasService.validarAccesoQR(req, "acceso-1"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(entradaRepository, never()).save(any());
    }

    // Verifica que un QR válido pero perteneciente a otro evento sea rechazado con 409 Conflict
    @Test
    void validarQrDeOtroEventoRechazaCon409Test() {
        Entrada entrada = Entrada.builder()
                .id(15L)
                .eventoId(99L)
                .sectorId(3L)
                .codigoQR("QR-OTRO-EVENTO")
                .estado(EstadoEntrada.VENDIDA)
                .build();

        when(entradaRepository.findByCodigoQRForUpdate("QR-OTRO-EVENTO"))
                .thenReturn(Optional.of(entrada));

        ValidarQrRequestDTO req = new ValidarQrRequestDTO();
        req.setEventoId(2L);
        req.setCodigoQR("QR-OTRO-EVENTO");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> ventasService.validarAccesoQR(req, "acceso-1"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    // Verifica que un QR inexistente sea rechazado con 404 Not Found
    @Test
    void validarQrInexistenteRechazaCon404Test() {
        when(entradaRepository.findByCodigoQRForUpdate("QR-NO-EXISTE"))
                .thenReturn(Optional.empty());

        ValidarQrRequestDTO req = new ValidarQrRequestDTO();
        req.setEventoId(2L);
        req.setCodigoQR("QR-NO-EXISTE");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> ventasService.validarAccesoQR(req, "acceso-1"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    // Verifica el cálculo de entradas vendidas, utilizadas, devueltas, ocupación y recaudación por sector (Req. 12 / US-22)
    @Test
    void reporteEventoCalculaOcupacionYRecaudacionPorSectorTest() {
        EventoCatalogoDTO.SectorCatalogoDTO platea = new EventoCatalogoDTO.SectorCatalogoDTO();
        platea.setId(10L);
        platea.setNombre("Platea");
        platea.setCapacidad(100);
        platea.setEsNumerado(true);

        EventoCatalogoDTO.PrecioSectorCatalogoDTO ps = new EventoCatalogoDTO.PrecioSectorCatalogoDTO();
        ps.setSector(platea);
        ps.setPrecio(new BigDecimal("20000"));

        EventoCatalogoDTO evento = new EventoCatalogoDTO();
        evento.setId(2L);
        evento.setNombre("Recital Tandil");
        evento.setOrganizadorId("org-1");
        evento.setEstado("PUBLICADO");
        evento.setFechaHora(LocalDateTime.now().plusDays(15));
        evento.setPreciosSectores(List.of(ps));

        Entrada vendida = Entrada.builder()
                .id(1L).eventoId(2L).sectorId(10L)
                .precio(new BigDecimal("20000")).estado(EstadoEntrada.VENDIDA).build();
        Entrada usada = Entrada.builder()
                .id(2L).eventoId(2L).sectorId(10L)
                .precio(new BigDecimal("20000")).estado(EstadoEntrada.USADA).build();
        Entrada devuelta = Entrada.builder()
                .id(3L).eventoId(2L).sectorId(10L)
                .precio(new BigDecimal("20000")).estado(EstadoEntrada.DEVUELTA).build();

        when(catalogoClient.obtenerEvento(2L)).thenReturn(evento);
        when(entradaRepository.findByEventoId(2L)).thenReturn(List.of(vendida, usada, devuelta));

        ReporteEventoDTO reporte = ventasService.obtenerReporteEvento(2L, "org-1", "ORGANIZADOR");

        assertEquals(2L, reporte.getTotalEntradasVendidas());
        assertEquals(1L, reporte.getTotalEntradasUtilizadas());
        assertEquals(1L, reporte.getTotalEntradasDevueltas());
        assertEquals(2.0, reporte.getPorcentajeOcupacionTotal());
        assertEquals(new BigDecimal("40000"), reporte.getRecaudacionTotal());
        assertEquals(1, reporte.getSectores().size());

        ReporteSectorDTO sectorReporte = reporte.getSectores().get(0);
        assertEquals("Platea", sectorReporte.getNombreSector());
        assertEquals(2L, sectorReporte.getEntradasVendidas());
        assertEquals(1L, sectorReporte.getEntradasDevueltas());
    }

    // Verifica que un organizador no pueda consultar el reporte de un evento de otro organizador (403 Forbidden)
    @Test
    void reporteEventoDeOtroOrganizadorRechazaCon403Test() {
        EventoCatalogoDTO evento = new EventoCatalogoDTO();
        evento.setId(2L);
        evento.setOrganizadorId("org-duenio");
        evento.setFechaHora(LocalDateTime.now().plusDays(15));

        when(catalogoClient.obtenerEvento(2L)).thenReturn(evento);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> ventasService.obtenerReporteEvento(2L, "org-intruso", "ORGANIZADOR"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
