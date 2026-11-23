package com.ticketya.catalogo;

import com.ticketya.catalogo.client.VentasClient;
import com.ticketya.catalogo.dto.EventoCreateDTO;
import com.ticketya.catalogo.dto.LoginRequestDTO;
import com.ticketya.catalogo.dto.LugarDTO;
import com.ticketya.catalogo.dto.NotificacionDTO;
import com.ticketya.catalogo.dto.PrecioSectorDTO;
import com.ticketya.catalogo.dto.PreferenciaClienteDTO;
import com.ticketya.catalogo.dto.SectorDTO;
import com.ticketya.catalogo.model.Evento;
import com.ticketya.catalogo.model.Lugar;
import com.ticketya.catalogo.model.PreferenciaClienteDocument;
import com.ticketya.catalogo.model.TipoEvento;
import com.ticketya.catalogo.repository.EventoRepository;
import com.ticketya.catalogo.repository.LugarRepository;
import com.ticketya.catalogo.repository.PreferenciaClienteNoSqlRepository;
import com.ticketya.catalogo.security.JwtAuthFilter;
import com.ticketya.catalogo.security.JwtUtil;
import com.ticketya.catalogo.service.EventoService;
import com.ticketya.catalogo.service.LugarService;
import com.ticketya.catalogo.service.NotificacionService;
import com.ticketya.catalogo.service.PreferenciaRecomendacionService;
import com.ticketya.catalogo.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de Entrega 2 en catalogo-service:
 * - Autenticación y validación de tokens JWT (HS256), expiración (401) y roles (403) (US-23).
 * - Persistencia documental NoSQL de preferencias de clientes (Req. 10 / US-19).
 * - Motor de recomendaciones personalizadas (Req. 10 / US-20).
 * - Notificaciones automáticas cuando un artista favorito publica evento en la ciudad del cliente (Req. 11 / US-21).
 */
@SpringBootTest
class Entrega2CatalogoTest {

    @Autowired private AuthController authController;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private PreferenciaRecomendacionService preferenciaService;
    @Autowired private PreferenciaClienteNoSqlRepository preferenciaRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private LugarService lugarService;
    @Autowired private EventoService eventoService;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private LugarRepository lugarRepository;

    @MockitoBean private VentasClient ventasClient;

    @BeforeEach
    void limpiar() {
        eventoRepository.deleteAll();
        lugarRepository.deleteAll();
        preferenciaRepository.deleteAll();
        notificacionService.limpiarBandeja();
    }

    @Test
    void loginEmiteJwtValidoYValidaClaimsPorRolTest() {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("org-1");
        login.setPassword("1234");
        login.setRol("ORGANIZADOR");

        Map<String, Object> body = authController.login(login).getBody();
        assertNotNull(body);
        String token = (String) body.get("token");
        assertNotNull(token);

        Map<String, Object> claims = jwtUtil.validarYExtraerClaims(token);
        assertEquals("org-1", claims.get("sub"));
        assertEquals("ORGANIZADOR", claims.get("role"));
    }

    @Test
    void loginConPasswordInvalidaRechazaCon401Test() {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("org-1");
        login.setPassword("invalida");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authController.login(login));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void filtroJwtRechazaTokenInvalidoOVencidoCon401Test() throws Exception {
        // 1. Token adulterado -> 401
        MockHttpServletRequest reqInvalido = new MockHttpServletRequest("POST", "/api/eventos");
        reqInvalido.addHeader("Authorization", "Bearer token.adulterado.invalido");
        MockHttpServletResponse resInvalido = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(reqInvalido, resInvalido, new MockFilterChain());
        assertEquals(401, resInvalido.getStatus());

        // 2. Token expirado -> 401
        String tokenExpirado = jwtUtil.generarTokenConExpiracion("cliente-1", "CLIENTE",
                Instant.now().getEpochSecond() - 60);
        MockHttpServletRequest reqExpirado = new MockHttpServletRequest("POST", "/api/eventos");
        reqExpirado.addHeader("Authorization", "Bearer " + tokenExpirado);
        MockHttpServletResponse resExpirado = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(reqExpirado, resExpirado, new MockFilterChain());
        assertEquals(401, resExpirado.getStatus());
    }

    @Test
    void preferenciasNoSqlRecomendacionesYNotificacionArtistaFavoritoTest() {
        // 1. El cliente guarda sus preferencias en la colección NoSQL
        PreferenciaClienteDTO prefDto = new PreferenciaClienteDTO();
        prefDto.setCiudad("Tandil");
        prefDto.setTiposEvento(List.of(TipoEvento.RECITAL));
        prefDto.setArtistasFavoritos(List.of("Los Piojos"));

        PreferenciaClienteDocument doc = preferenciaService.guardarPreferencias("cliente-fan", prefDto);
        assertNotNull(doc.getId());
        assertEquals("Tandil", doc.getCiudad());

        // 2. Se crean dos lugares (Tandil y Azul) y dos eventos
        Lugar tandil = crearLugar("Anfiteatro Tandil", "Tandil");
        Lugar azul = crearLugar("Teatro Azul", "Azul");

        Evento eventoOtro = crearYPublicarEvento("Obra de Teatro", TipoEvento.TEATRO,
                List.of("Actor X"), azul);
        Evento eventoFavorito = crearYPublicarEvento("Los Piojos en Tandil", TipoEvento.RECITAL,
                List.of("Los Piojos"), tandil);

        // 3. El motor de recomendaciones debe priorizar el recital de su artista favorito en su ciudad
        List<Evento> recomendados = preferenciaService.recomendarEventos("cliente-fan");
        assertEquals(2, recomendados.size());
        assertEquals(eventoFavorito.getId(), recomendados.get(0).getId());
        assertEquals(eventoOtro.getId(), recomendados.get(1).getId());

        // 4. Al publicarse el evento de Los Piojos en Tandil, el cliente recibió la notificación PUSH (Req. 11)
        List<NotificacionDTO> notificaciones = notificacionService.listarNotificacionesCliente("cliente-fan");
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).getAsunto().contains("Los Piojos"));
    }

    private Lugar crearLugar(String nombre, String ciudad) {
        SectorDTO sector = new SectorDTO();
        sector.setNombre("Campo");
        sector.setCapacidad(100);
        sector.setEsNumerado(false);

        LugarDTO dto = new LugarDTO();
        dto.setNombre(nombre);
        dto.setDireccion("Calle 123");
        dto.setCiudad(ciudad);
        dto.setSectores(List.of(sector));

        return lugarService.crearLugar(dto);
    }

    private Evento crearYPublicarEvento(String nombre, TipoEvento tipo, List<String> participantes, Lugar lugar) {
        PrecioSectorDTO precio = new PrecioSectorDTO();
        precio.setSectorId(lugar.getSectores().get(0).getId());
        precio.setPrecio(new BigDecimal("15000"));

        EventoCreateDTO dto = new EventoCreateDTO();
        dto.setNombre(nombre);
        dto.setDescripcion("Evento test");
        dto.setTipo(tipo);
        dto.setParticipantes(participantes);
        dto.setFechaHora(LocalDateTime.now().plusDays(20));
        dto.setLugarId(lugar.getId());
        dto.setPrecios(List.of(precio));

        Evento borrador = eventoService.crearEventoBorrador(dto, "org-1");
        return eventoService.publicarEvento(borrador.getId(), "org-1");
    }
}
