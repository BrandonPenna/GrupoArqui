package com.ticketya.ventas_service.concurrencia;

import com.ticketya.ventas_service.client.CatalogoClient;
import com.ticketya.ventas_service.dto.AsientoPedidoDTO;
import com.ticketya.ventas_service.dto.ConfirmarCompraDTO;
import com.ticketya.ventas_service.dto.EventoCatalogoDTO;
import com.ticketya.ventas_service.dto.ReservaCreateDTO;
import com.ticketya.ventas_service.model.CupoSector;
import com.ticketya.ventas_service.model.EstadoEntrada;
import com.ticketya.ventas_service.model.Reserva;
import com.ticketya.ventas_service.repository.*;
import com.ticketya.ventas_service.service.VentasServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Prueba de NO doble venta: muchos clientes intentan comprar el mismo lugar exactamente al mismo tiempo.
 * Se usa el contexto completo de Spring con una base H2 real (no mocks de repositorio),
 * así la restricción UNIQUE y el bloqueo FOR UPDATE se prueban de verdad.
 * Solo el catálogo se simula, porque es otro microservicio.
 */
@SpringBootTest
class DobleVentaConcurrenteTest {

    private static final int CLIENTES = 50;
    private static final long EVENTO = 1L;
    private static final long PLATEA = 10L;   // sector numerado
    private static final long CAMPO = 20L;    // sector no numerado, capacidad 5

    @Autowired private VentasServices ventas;
    @Autowired private EntradaRepository entradaRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private DevolucionRepository devolucionRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private CupoSectorRepository cupoSectorRepository;

    @MockitoBean private CatalogoClient catalogoClient;

    @BeforeEach
    void preparar() {
        devolucionRepository.deleteAll();
        compraRepository.deleteAll();
        reservaRepository.deleteAll();
        cupoSectorRepository.deleteAll();
        EventoCatalogoDTO evento = new EventoCatalogoDTO();
        evento.setFechaHora(LocalDateTime.now().plusDays(30));
        when(catalogoClient.obtenerEvento(anyLong())).thenReturn(evento);
                evento.setEstado("PUBLICADO");
        evento.getPreciosSectores().add(precioSector(PLATEA, true, 100, "20000"));
        evento.getPreciosSectores().add(precioSector(CAMPO, false, 5, "10000"));
    }

    // 50 clientes piden la fila 1 asiento 1 al mismo tiempo: solo uno lo consigue.
    @Test
    void muchasComprasSimultaneasDelMismoAsiento() throws Exception {
        Resultado r = ejecutarEnParalelo(() -> pedidoNumerado(1, 1));

        assertEquals(1, r.exitos.get(), "Solo una reserva del asiento debe ser exitosa");
        assertEquals(CLIENTES - 1, r.rechazos.get());
        assertEquals(1, entradaRepository.findOcupadasSector(EVENTO, PLATEA,
                List.of(EstadoEntrada.RESERVADA, EstadoEntrada.VENDIDA)).size());
    }

    // Campo con capacidad 5 y 4 lugares ya ocupados: de 50 clientes simultáneos, solo uno obtiene el último lugar.
    @Test
    void muchasComprasSimultaneasDelUltimoLugarDeUnaZona() throws Exception {
        ventas.crearReservaTemporal(pedidoCampo(4), "cliente-previo");

        Resultado r = ejecutarEnParalelo(() -> pedidoCampo(1));

        assertEquals(1, r.exitos.get(), "Solo un cliente debe obtener el último lugar");
        CupoSector cupo = cupoSectorRepository.findAll().get(0);
        assertEquals(5, cupo.getOcupados());
    }

    // Un pago rechazado libera el asiento: después otro cliente lo puede reservar.
    @Test
    void pagoRechazadoLiberaElAsiento() {
        Reserva reserva = ventas.crearReservaTemporal(pedidoNumerado(3, 3), "cliente-a");
        ConfirmarCompraDTO pago = new ConfirmarCompraDTO();
        pago.setReservaId(reserva.getId());
        pago.setMedioPago("RECHAZAR");
        try { ventas.confirmarCompra(pago, "cliente-a"); } catch (Exception esperado) { }

        Reserva otra = ventas.crearReservaTemporal(pedidoNumerado(3, 3), "cliente-b");
        assertEquals(1, otra.getEntradas().size());
    }

    // El mismo cliente aprieta "Pagar" 10 veces a la vez (reintentos por corte de conexión): una sola compra.
    @Test
    void reintentosSimultaneosDelPagoGeneranUnaSolaCompra() throws Exception {
        Reserva reserva = ventas.crearReservaTemporal(pedidoNumerado(2, 2), "cliente-x");

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Long>> futuros = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                ConfirmarCompraDTO c = new ConfirmarCompraDTO();
                c.setReservaId(reserva.getId());
                c.setMedioPago("TARJETA_CREDITO");
                return ventas.confirmarCompra(c, "cliente-x").getId();
            }));
        }
        largada.countDown();
        List<Long> ids = new ArrayList<>();
        for (Future<Long> f : futuros) ids.add(f.get(30, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(1, compraRepository.count(), "Debe existir una única compra");
        assertEquals(1, ids.stream().distinct().count(), "Todos los reintentos devuelven la misma compra");
    }

    // ---------------------------------------------------------------------------------

    private record Resultado(AtomicInteger exitos, AtomicInteger rechazos) {}

    private Resultado ejecutarEnParalelo(Supplier<ReservaCreateDTO> pedido) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(CLIENTES);
        CountDownLatch listos = new CountDownLatch(CLIENTES);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazos = new AtomicInteger();
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < CLIENTES; i++) {
            final int n = i;
            futuros.add(pool.submit(() -> {
                listos.countDown();
                largada.await();               // todos arrancan juntos
                try {
                    ventas.crearReservaTemporal(pedido.get(), "cliente-" + n);
                    exitos.incrementAndGet();
                } catch (Exception e) {
                    rechazos.incrementAndGet();
                }
                return null;
            }));
        }
        listos.await();
        largada.countDown();
        for (Future<?> f : futuros) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();
        return new Resultado(exitos, rechazos);
    }

    private ReservaCreateDTO pedidoNumerado(int fila, int asiento) {
        AsientoPedidoDTO a = new AsientoPedidoDTO();
        a.setFila(fila);
        a.setAsiento(asiento);
        a.setPrecio(new BigDecimal("20000"));
        ReservaCreateDTO dto = new ReservaCreateDTO();
        dto.setEventoId(EVENTO);
        dto.setSectorId(PLATEA);
        dto.setEsNumerado(true);
        dto.setAsientos(List.of(a));
        return dto;
    }

    private ReservaCreateDTO pedidoCampo(int cantidad) {
        List<AsientoPedidoDTO> lugares = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            AsientoPedidoDTO a = new AsientoPedidoDTO();
            a.setPrecio(new BigDecimal("10000"));
            lugares.add(a);
        }
        ReservaCreateDTO dto = new ReservaCreateDTO();
        dto.setEventoId(EVENTO);
        dto.setSectorId(CAMPO);
        dto.setEsNumerado(false);
        dto.setCapacidadMaximaSector(5);
        dto.setAsientos(lugares);
        return dto;
    }

        private EventoCatalogoDTO.PrecioSectorCatalogoDTO precioSector(Long id, boolean numerado, int capacidad, String precio) {
        EventoCatalogoDTO.SectorCatalogoDTO s = new EventoCatalogoDTO.SectorCatalogoDTO();
        s.setId(id);
        s.setEsNumerado(numerado);
        s.setCapacidad(capacidad);
        s.setFilas(100);
        s.setAsientosPorFila(100);
        EventoCatalogoDTO.PrecioSectorCatalogoDTO ps = new EventoCatalogoDTO.PrecioSectorCatalogoDTO();
        ps.setSector(s);
        ps.setPrecio(new BigDecimal(precio));
        return ps;
    }
}
