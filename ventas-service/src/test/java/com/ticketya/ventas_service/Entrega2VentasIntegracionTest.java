package com.ticketya.ventas_service;

import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.controller.AccesoController;
import com.ticketya.ventas_service.controller.ReporteController;
import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.EstadoFilaVirtualDTO;
import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.dto.ValidarQrRequestDTO;
import com.ticketya.ventas_service.model.Compra;
import com.ticketya.ventas_service.model.Reserva;
import com.ticketya.ventas_service.repository.CompraRepository;
import com.ticketya.ventas_service.repository.CupoSectorRepository;
import com.ticketya.ventas_service.repository.DevolucionRepository;
import com.ticketya.ventas_service.repository.ReservaRepository;
import com.ticketya.ventas_service.security.JwtAuthFilter;
import com.ticketya.ventas_service.security.JwtUtil;
import com.ticketya.ventas_service.service.FilaVirtualService;
import com.ticketya.ventas_service.service.VentasServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Pruebas integrales de Entrega 2 en ventas-service:
 * - Fila virtual por orden de llegada y tandas para eventos de alta demanda (Req. 6 / US-17).
 * - Resiliencia en pasarela de pagos: reintentos automáticos ante fallo transitorio y fallback ante caída (Entrega 2).
 * - Seguridad JWT y control de acceso por roles en endpoints de QR y Reportes (US-23).
 */
@SpringBootTest
class Entrega2VentasIntegracionTest {

    @Autowired private VentasServices ventasService;
    @Autowired private FilaVirtualService filaVirtualService;
    @Autowired private AccesoController accesoController;
    @Autowired private ReporteController reporteController;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private DevolucionRepository devolucionRepository;
    @Autowired private CupoSectorRepository cupoSectorRepository;

    @MockitoBean private CatalogoClient catalogoClient;

    @BeforeEach
    void preparar() {
        devolucionRepository.deleteAll();
        compraRepository.deleteAll();
        reservaRepository.deleteAll();
        cupoSectorRepository.deleteAll();
        filaVirtualService.limpiarTodo();
    }

    @Test
    void filaVirtualEnEventoDeAltaDemandaHabilitaPorTandasTest() {
        EventoCatalogoDTO eventoAltaDemanda = crearEventoCatalogo(true);
        when(catalogoClient.obtenerEvento(anyLong())).thenReturn(eventoAltaDemanda);

        ReservaCreateDTO pedido = crearPedidoNumerado(1, 1);

        // 1. Si el cliente intenta reservar sin haber ingresado a la fila virtual -> 409 Conflict
        ResponseStatusException sinFila = assertThrows(ResponseStatusException.class,
                () -> ventasService.crearReservaTemporal(pedido, "cliente-1"));
        assertEquals(HttpStatus.CONFLICT, sinFila.getStatusCode());

        // 2. Ingresan 6 clientes en orden (el tamaño de tanda es 5)
        for (int i = 1; i <= 6; i++) {
            EstadoFilaVirtualDTO estado = filaVirtualService.ingresarAFila(1L, "cliente-" + i);
            assertEquals(i, estado.getPosicion());
            assertEquals(i <= FilaVirtualService.TAMANIO_TANDA_DEFAULT, estado.isHabilitadoParaComprar());
        }

        // 3. El cliente-6 (posición 6, fuera de la tanda 1..5) intenta reservar y es rechazado con 409
        ResponseStatusException fueraDeTanda = assertThrows(ResponseStatusException.class,
                () -> ventasService.crearReservaTemporal(crearPedidoNumerado(1, 2), "cliente-6"));
        assertEquals(HttpStatus.CONFLICT, fueraDeTanda.getStatusCode());

        // 4. El cliente-1 (posición 1, dentro de la tanda activa) reserva con éxito y libera su lugar en la fila
        Reserva reservaCliente1 = ventasService.crearReservaTemporal(pedido, "cliente-1");
        assertNotNull(reservaCliente1.getId());

        // 5. Al consumirse el turno del cliente-1, el cliente-6 sube a la posición 5 (entra en la tanda) y ya puede reservar
        EstadoFilaVirtualDTO nuevoEstado6 = filaVirtualService.consultarEstado(1L, "cliente-6");
        assertEquals(5, nuevoEstado6.getPosicion());
        assertTrue(nuevoEstado6.isHabilitadoParaComprar());

        Reserva reservaCliente6 = ventasService.crearReservaTemporal(crearPedidoNumerado(1, 2), "cliente-6");
        assertNotNull(reservaCliente6.getId());
    }

    @Test
    void resilienciaPasarelaReintentaFalloTransitorioYActivaFallbackEnTimeoutTest() {
        when(catalogoClient.obtenerEvento(anyLong())).thenReturn(crearEventoCatalogo(false));

        // 1. Fallo transitorio: falla en el intento 1 pero el reintento automático lo recupera con éxito
        Reserva r1 = ventasService.crearReservaTemporal(crearPedidoNumerado(2, 1), "cliente-1");
        ConfirmarCompraDTO pagoTransitorio = new ConfirmarCompraDTO();
        pagoTransitorio.setReservaId(r1.getId());
        pagoTransitorio.setMedioPago("FALLO_TRANSITORIO");

        Compra compraExitosa = ventasService.confirmarCompra(pagoTransitorio, "cliente-1");
        assertNotNull(compraExitosa.getId());
        assertTrue(compraExitosa.getTransaccionExternaId().startsWith("PAY-"));

        // 2. Timeout persistente: agota los 3 reintentos, activa fallback liberando el asiento y responde 402
        Reserva r2 = ventasService.crearReservaTemporal(crearPedidoNumerado(2, 2), "cliente-2");
        ConfirmarCompraDTO pagoTimeout = new ConfirmarCompraDTO();
        pagoTimeout.setReservaId(r2.getId());
        pagoTimeout.setMedioPago("TIMEOUT");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ventasService.confirmarCompra(pagoTimeout, "cliente-2"));
        assertEquals(HttpStatus.PAYMENT_REQUIRED, ex.getStatusCode());

        // El asiento (2, 2) quedó libre gracias al fallback compensatorio
        Reserva r3 = ventasService.crearReservaTemporal(crearPedidoNumerado(2, 2), "cliente-3");
        assertNotNull(r3.getId());
    }

    @Test
    void seguridadPorRolesYJwtEnEndpointsDeVentasTest() throws Exception {
        // 1. Token JWT inválido es rechazado con 401 por JwtAuthFilter
        MockHttpServletRequest reqInvalido = new MockHttpServletRequest("POST", "/api/accesos/validar-qr");
        reqInvalido.addHeader("Authorization", "Bearer token.falso.123");
        MockHttpServletResponse resInvalido = new MockHttpServletResponse();
        jwtAuthFilter.doFilter(reqInvalido, resInvalido, new MockFilterChain());
        assertEquals(401, resInvalido.getStatus());

        // 2. Un CLIENTE no puede escanear QR en el control de acceso (403 Forbidden)
        ValidarQrRequestDTO qrReq = new ValidarQrRequestDTO();
        qrReq.setEventoId(1L);
        qrReq.setCodigoQR("QR-123");
        ResponseEntity<?> resAccesoCliente = accesoController.validarQr(qrReq, "cliente-1", "CLIENTE");
        assertEquals(HttpStatus.FORBIDDEN, resAccesoCliente.getStatusCode());

        // 3. Un CLIENTE no puede consultar reportes de organizadores (403 Forbidden)
        ResponseEntity<?> resReporteCliente = reporteController.obtenerReporteEvento(1L, "cliente-1", "CLIENTE");
        assertEquals(HttpStatus.FORBIDDEN, resReporteCliente.getStatusCode());

        // 4. Token JWT válido inyecta correctamente el usuario y rol en el request
        String tokenPersonal = jwtUtil.generarToken("portero-1", "PERSONAL_ACCESO");
        MockHttpServletRequest reqValido = new MockHttpServletRequest("POST", "/api/accesos/validar-qr");
        reqValido.addHeader("Authorization", "Bearer " + tokenPersonal);
        MockHttpServletResponse resValido = new MockHttpServletResponse();

        final String[] rolCapturado = new String[1];
        jwtAuthFilter.doFilter(reqValido, resValido, (request, response) -> {
            rolCapturado[0] = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("X-User-Role");
        });
        assertEquals("PERSONAL_ACCESO", rolCapturado[0]);
    }

    private EventoCatalogoDTO crearEventoCatalogo(boolean altaDemanda) {
        EventoCatalogoDTO.SectorCatalogoDTO sector = new EventoCatalogoDTO.SectorCatalogoDTO();
        sector.setId(10L);
        sector.setNombre("Platea");
        sector.setEsNumerado(true);
        sector.setCapacidad(100);
        sector.setFilas(10);
        sector.setAsientosPorFila(10);

        EventoCatalogoDTO.PrecioSectorCatalogoDTO ps = new EventoCatalogoDTO.PrecioSectorCatalogoDTO();
        ps.setSector(sector);
        ps.setPrecio(new BigDecimal("15000"));

        EventoCatalogoDTO evento = new EventoCatalogoDTO();
        evento.setId(1L);
        evento.setNombre("Recital Alta Demanda");
        evento.setOrganizadorId("org-1");
        evento.setEstado("PUBLICADO");
        evento.setAltaDemanda(altaDemanda);
        evento.setFechaHora(LocalDateTime.now().plusDays(20));
        evento.getPreciosSectores().add(ps);
        return evento;
    }

    private ReservaCreateDTO crearPedidoNumerado(int fila, int asiento) {
        AsientoPedidoDTO a = new AsientoPedidoDTO();
        a.setFila(fila);
        a.setAsiento(asiento);

        ReservaCreateDTO dto = new ReservaCreateDTO();
        dto.setEventoId(1L);
        dto.setSectorId(10L);
        dto.setAsientos(List.of(a));
        return dto;
    }
}
