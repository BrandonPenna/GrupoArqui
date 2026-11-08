package com.ticketya.ventas_service.service;

import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.SolicitudDevolucionDTO;
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
}