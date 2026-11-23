package com.ticketya.catalogo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Filtro de autenticación y autorización JWT (US-23).
 * - Si la petición incluye "Authorization: Bearer <token>", valida firma HS256 y expiración.
 *   Si es inválido o expirado responde 401 Unauthorized.
 *   Si es válido, inyecta los claims "sub" y "role" en X-User-Id y X-User-Role.
 * - Si se envía "X-Require-Jwt: true" sin token Bearer, responde 401 Unauthorized.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String requireJwt = request.getHeader("X-Require-Jwt");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                Map<String, Object> claims = jwtUtil.validarYExtraerClaims(token);
                String userId = String.valueOf(claims.get("sub"));
                String role = String.valueOf(claims.get("role"));

                HttpServletRequest wrapped = new JwtHeaderRequestWrapper(request, userId, role);
                filterChain.doFilter(wrapped, response);
                return;
            } catch (IllegalArgumentException ex) {
                escribirError401(response, request.getRequestURI(), ex.getMessage());
                return;
            }
        } else if (authHeader != null && !authHeader.isBlank()) {
            escribirError401(response, request.getRequestURI(), "Esquema de autorización inválido. Use Bearer <token>");
            return;
        } else if ("true".equalsIgnoreCase(requireJwt)) {
            escribirError401(response, request.getRequestURI(), "Falta el token JWT de autenticación");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void escribirError401(HttpServletResponse response, String uri, String detalle) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = String.format(
                "{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"%s\",\"instance\":\"%s\"}",
                detalle.replace("\"", "'"), uri);
        response.getWriter().write(json);
    }

    private static class JwtHeaderRequestWrapper extends HttpServletRequestWrapper {
        private final String userId;
        private final String role;

        public JwtHeaderRequestWrapper(HttpServletRequest request, String userId, String role) {
            super(request);
            this.userId = userId;
            this.role = role;
        }

        @Override
        public String getHeader(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) {
                return userId;
            }
            if ("X-User-Role".equalsIgnoreCase(name)) {
                return role;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(userId));
            }
            if ("X-User-Role".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(role));
            }
            return super.getHeaders(name);
        }
    }
}
