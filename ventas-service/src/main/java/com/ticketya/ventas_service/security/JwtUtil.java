package com.ticketya.ventas_service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilidad para emisión y validación de tokens JWT estándar (RFC 7519) firmados con HMAC-SHA256 (HS256).
 */
@Component
public class JwtUtil {

    private static final String SECRET = "ticketya-super-secret-jwt-key-2026-arquitectura-web-unicen";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long TTL_SEGUNDOS = 3600; // 1 hora

    private final ObjectMapper mapper = new ObjectMapper();

    public String generarToken(String userId, String rol) {
        return generarTokenConExpiracion(userId, rol, Instant.now().getEpochSecond() + TTL_SEGUNDOS);
    }

    public String generarTokenConExpiracion(String userId, String rol, long expEpochSeconds) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", userId);
            payload.put("role", rol.toUpperCase());
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("exp", expEpochSeconds);

            String headerB64 = base64UrlEncode(mapper.writeValueAsBytes(header));
            String payloadB64 = base64UrlEncode(mapper.writeValueAsBytes(payload));
            String signingInput = headerB64 + "." + payloadB64;
            String signatureB64 = firmar(signingInput);

            return signingInput + "." + signatureB64;
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar token JWT", e);
        }
    }

    public Map<String, Object> validarYExtraerClaims(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato de token JWT inválido");
            }
            String signingInput = partes[0] + "." + partes[1];
            String firmaEsperada = firmar(signingInput);
            if (!firmaEsperada.equals(partes[2])) {
                throw new IllegalArgumentException("Firma de token JWT inválida");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(partes[1]);
            Map<String, Object> claims = mapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});

            Object expObj = claims.get("exp");
            if (expObj instanceof Number expNum) {
                if (Instant.now().getEpochSecond() > expNum.longValue()) {
                    throw new IllegalArgumentException("El token JWT ha expirado");
                }
            } else {
                throw new IllegalArgumentException("El token JWT no contiene claim exp");
            }

            return claims;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Token JWT malformado o inválido", e);
        }
    }

    private String firmar(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
        return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
