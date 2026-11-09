package com.ticketya.catalogo.repository;

import com.ticketya.catalogo.dto.EventoFiltroDTO;
import com.ticketya.catalogo.model.EstadoEvento;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.Lugar;
import com.ticketya.catalogo.model.TipoEvento;
import com.ticketya.catalogo.specification.EventoSpecification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventoRepositoryBusquedaTest {

        @Autowired
        private EventoRepository eventoRepository;

        @Autowired
        private EntityManager entityManager;

        //Busqueda con filtros de ciudad, tipo y participante
        @Test
        void busquedaCiudadTipoParticipanteTest() {
                crearEvento("Recital esperado", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otro tipo", "Tandil", TipoEvento.TEATRO,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otra ciudad", "Azul", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Borrador", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.BORRADOR);

                entityManager.flush();
                entityManager.clear();

                EventoFiltroDTO filtro = new EventoFiltroDTO();
                filtro.setCiudad("TANDIL");
                filtro.setTipo(TipoEvento.RECITAL);
                filtro.setParticipante("Banda A");

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        PageRequest.of(0, 10)
                );

                assertThat(resultado.getContent())
                        .extracting(Evento::getNombre)
                        .containsExactly("Recital esperado");
        }

        //Busqueda ordenada por fecha y paginada
        @Test   
        void busquedaOrdenadaPaginadaTest(){
                crearEvento("Evento tercero", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 20, 20, 0));

                crearEvento("Evento primero", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 1, 20, 0));

                crearEvento("Evento segundo", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 10, 20, 0));

                entityManager.flush();
                entityManager.clear();

                EventoFiltroDTO filtro = new EventoFiltroDTO();

                PageRequest primeraPagina = PageRequest.of(
                        0, 2, Sort.by(Sort.Direction.ASC, "fechaHora"));
                Page<Evento> resultadoPrimeraPagina = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        primeraPagina
                );

                assertThat(resultadoPrimeraPagina.getContent())
                .extracting(Evento::getNombre)
                .containsExactly("Evento primero", "Evento segundo");
                assertThat(resultadoPrimeraPagina.getTotalElements()).isEqualTo(3);
                assertThat(resultadoPrimeraPagina.getTotalPages()).isEqualTo(2);

                PageRequest segundaPagina = PageRequest.of(
                        1, 2, Sort.by(Sort.Direction.ASC, "fechaHora"));

                Page<Evento> resultadoSegundaPagina = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        segundaPagina
                );

                assertThat(resultadoSegundaPagina.getContent())
                        .extracting(Evento::getNombre)
                        .containsExactly("Evento tercero");
        }

        //Busqueda por organizador
        @Test
        void busquedaPorOrganizadorTest() {
                crearEvento("Recital esperado", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otro tipo", "Tandil", TipoEvento.TEATRO,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otra ciudad", "Azul", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Borrador", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.BORRADOR);

                Evento otroOrganizador = crearEvento("Otra ciudad", "Azul", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO
                );
                otroOrganizador.setOrganizadorId("organizador-2");

                entityManager.flush();
                entityManager.clear();

                EventoFiltroDTO filtro = new EventoFiltroDTO();
                filtro.setOrganizadorId("organizador-1");

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        PageRequest.of(0, 10)
                );

                assertThat(resultado.getContent())
                .hasSize(3)
                .allMatch(evento ->
                        evento.getEstado() == EstadoEvento.PUBLICADO
                        && evento.getOrganizadorId().equals("organizador-1"));
        }

        //Busqueda por rango de fechas
        @Test
        void busquedaRangoFechasTest(){
                crearEvento("Antes del rango", "Tandil", TipoEvento.RECITAL,
                "Banda A", EstadoEvento.PUBLICADO,
                LocalDateTime.of(2026, 12, 1, 20, 0));

                crearEvento("Dentro del rango", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 10, 20, 0));

                crearEvento("Después del rango", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 20, 20, 0));

                entityManager.flush();
                entityManager.clear();

                EventoFiltroDTO filtro = new EventoFiltroDTO();
                filtro.setFechaDesde(LocalDateTime.of(2026, 12, 5, 0, 0));
                filtro.setFechaHasta(LocalDateTime.of(2026, 12, 15, 23, 59));

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        PageRequest.of(0, 10)
                );

                assertThat(resultado.getContent())
                .extracting(Evento::getNombre)
                .containsExactly("Dentro del rango");
        }

        //Busqueda sin filtros, solo eventos publicados
        @Test
        void busquedaSinFiltrosTest() {
                crearEvento("Recital esperado", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otro tipo", "Tandil", TipoEvento.TEATRO,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Otra ciudad", "Azul", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.PUBLICADO);

                crearEvento("Borrador", "Tandil", TipoEvento.RECITAL,
                        "Banda A", EstadoEvento.BORRADOR);

                entityManager.flush();
                entityManager.clear();
                
                EventoFiltroDTO filtro = new EventoFiltroDTO();

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro),
                        PageRequest.of(0, 10)
                );

                assertThat(resultado.getContent())
                        .hasSize(3)
                        .allMatch(evento -> evento.getEstado() == EstadoEvento.PUBLICADO);
        }
        
        @Test
        void busquedaOrdenadaPorPrecioTest() {
                crearEvento("Caro", "Tandil", TipoEvento.RECITAL, "Banda A", EstadoEvento.PUBLICADO)
                        .setPrecioDesde(new BigDecimal("50000"));
                crearEvento("Barato", "Tandil", TipoEvento.RECITAL, "Banda A", EstadoEvento.PUBLICADO)
                        .setPrecioDesde(new BigDecimal("5000"));
                crearEvento("Medio", "Tandil", TipoEvento.RECITAL, "Banda A", EstadoEvento.PUBLICADO)
                        .setPrecioDesde(new BigDecimal("20000"));
                entityManager.flush();
                entityManager.clear();

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(new EventoFiltroDTO()),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "precioDesde")));

                assertThat(resultado.getContent())
                        .extracting(Evento::getNombre)
                        .containsExactly("Barato", "Medio", "Caro");
        }

        @Test
        void busquedaPorRelevanciaTest() {
                crearEvento("Festival de rock", "Tandil", TipoEvento.FESTIVAL, "Piojos", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 1, 20, 0));
                crearEvento("Noche de cumbia", "Tandil", TipoEvento.RECITAL, "Los Palmeras", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 11, 20, 20, 0));
                crearEvento("Piojos en vivo", "Tandil", TipoEvento.RECITAL, "Los Piojos", EstadoEvento.PUBLICADO,
                        LocalDateTime.of(2026, 12, 30, 20, 0));
                entityManager.flush();
                entityManager.clear();

                EventoFiltroDTO filtro = new EventoFiltroDTO();
                filtro.setQ("piojos");

                Page<Evento> resultado = eventoRepository.findAll(
                        EventoSpecification.conFiltros(filtro)
                                .and(EventoSpecification.ordenadoPorRelevancia("piojos")),
                        PageRequest.of(0, 10));

                assertThat(resultado.getContent())
                        .extracting(Evento::getNombre)
                        .containsExactly("Piojos en vivo", "Festival de rock");
                assertThat(resultado.getTotalElements()).isEqualTo(2);
        }

        private Evento crearEvento(String nombre,String ciudad,TipoEvento tipo,String participante,EstadoEvento estado) {
                return crearEvento(nombre,ciudad,tipo,participante,
                        estado,LocalDateTime.of(2026, 12, 1, 20, 0)
                );
        }

        private Evento crearEvento(String nombre,String ciudad,TipoEvento tipo,String participante,EstadoEvento estado,LocalDateTime fechaHora) {
                Lugar lugar = Lugar.builder()
                        .nombre("Lugar " + ciudad)
                        .direccion("Calle 123")
                        .ciudad(ciudad)
                        .build();

                entityManager.persist(lugar);

                Evento evento = Evento.builder()
                        .nombre(nombre)
                        .tipo(tipo)
                        .organizadorId("organizador-1")
                        .participantes(List.of(participante))
                        .fechaHora(fechaHora)
                        .lugar(lugar)
                        .estado(estado)
                        .build();

                return eventoRepository.save(evento);
        }
}
