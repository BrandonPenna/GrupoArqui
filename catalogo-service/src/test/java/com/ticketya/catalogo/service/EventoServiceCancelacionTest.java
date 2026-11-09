package com.ticketya.catalogo.service;

import com.ticketya.catalogo.client.VentasClient;
import com.ticketya.catalogo.model.EstadoEvento;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.TipoEvento;
import com.ticketya.catalogo.repository.EventoRepository;
import com.ticketya.catalogo.repository.LugarRepository;
import com.ticketya.catalogo.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceCancelacionTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private LugarRepository lugarRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private VentasClient ventasClient;

    @InjectMocks
    private EventoService eventoService;

    @Test
    void cancelarEventoPublicadoAvisaAVentasTest() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento(EstadoEvento.PUBLICADO)));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        Evento cancelado = eventoService.cancelarEvento(1L, "admin-1", "ADMIN");

        assertEquals(EstadoEvento.CANCELADO, cancelado.getEstado());
        verify(ventasClient).procesarCancelacion(1L);
    }

    @Test
    void siVentasNoRespondeElEventoNoSeCancelaTest() {
        Evento evento = evento(EstadoEvento.PUBLICADO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE))
                .when(ventasClient).procesarCancelacion(1L);

        assertThrows(ResponseStatusException.class,
                () -> eventoService.cancelarEvento(1L, "admin-1", "ADMIN"));

        assertEquals(EstadoEvento.PUBLICADO, evento.getEstado());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void cancelarBorradorNoLlamaAVentasTest() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento(EstadoEvento.BORRADOR)));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(EstadoEvento.CANCELADO, eventoService.cancelarEvento(1L, "org-1", "ORGANIZADOR").getEstado());
        verifyNoInteractions(ventasClient);
    }

    @Test
    void organizadorNoPuedeCancelarEventoAjenoTest() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento(EstadoEvento.PUBLICADO)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> eventoService.cancelarEvento(1L, "org-intruso", "ORGANIZADOR"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(ventasClient);
    }

    @Test
    void cancelarEventoYaCanceladoTest() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento(EstadoEvento.CANCELADO)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> eventoService.cancelarEvento(1L, "admin-1", "ADMIN"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    private Evento evento(EstadoEvento estado) {
        return Evento.builder()
                .id(1L)
                .nombre("Recital")
                .tipo(TipoEvento.RECITAL)
                .organizadorId("org-1")
                .fechaHora(LocalDateTime.now().plusDays(30))
                .estado(estado)
                .build();
    }
}
