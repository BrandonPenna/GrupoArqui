# Diseño Técnico: Core del Sistema TicketYa

## 1. Arquitectura de Microservicios

El sistema se compone de tres componentes principales comunicados de forma síncrona mediante REST:

- **API Gateway (`api-gateway`):**
  - Desarrollado con Spring Cloud Gateway.
  - Expone un puerto único (`8080`) y enruta hacia los servicios internos.
  - Propaga headers de identidad simulada (`X-User-Id`, `X-User-Role`).

- **Servicio de Catálogo (`catalogo-service`):**
  - Puerto interno: `8081`.
  - Base de datos propia: PostgreSQL (`db_catalogo`).
  - Responsabilidad: Gestión de Lugares, Sectores (numerados y generales) y Eventos. Búsqueda multicriterio con JPA Specifications.

- **Servicio de Ventas y Reservas (`ventas-service`):**
  - Puerto interno: `8082`.
  - Base de datos propia: PostgreSQL (`db_ventas`).
  - Responsabilidad: Control de asientos, reservas temporales (10 min), emisión de tickets QR, cancelaciones, devoluciones y pasarela de pagos simulada (mock).

---

## 2. Mecanismo contra la Doble Venta y Concurrencia

Para garantizar que un asiento numerado o el cupo de un sector no numerado nunca se venda dos veces bajo alta concurrencia:

1. **Bloqueo Pesimista en Base de Datos:**
   - La consulta del asiento o sector para reservar se ejecuta con `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`).
2. **Evaluación de Estado Transaccional:**
   - Dentro de la misma transacción `@Transactional`:
     - Se verifica si el asiento está `DISPONIBLE`.
     - Si está `RESERVADO`, se compara `reserva_expira_en` con el timestamp actual. Si ya transcurrieron 10 minutos, se considera expirado y se permite la nueva reserva.
     - Si está `VENDIDO` o con reserva vigente, se rechaza la operación inmediatamente arrojando `ConflictoAsientoException` (HTTP 409).
3. **Persistencia de Reserva:**
   - Se actualiza el estado a `RESERVADO` y se fija `reserva_expira_en = NOW() + 10 MINUTOS`.

---

## 3. Modelo de Datos por Servicio

### Catálogo (`db_catalogo`)
- **`lugares`**: `id`, `nombre`, `direccion`, `latitud`, `longitud`.
- **`sectores`**: `id`, `lugar_id`, `nombre`, `tipo` (NUMERADO/NO_NUMERADO), `capacidad_maxima`.
- **`eventos`**: `id`, `nombre`, `descripcion`, `tipo`, `organizador_id`, `participantes`, `fecha_hora`, `lugar_id`, `estado` (BORRADOR, PUBLICADO, CANCELADO, FINALIZADO).
- **`precios_sector`**: `id`, `evento_id`, `sector_id`, `precio`.

### Ventas (`db_ventas`)
- **`asientos_inventario`**: `id`, `evento_id`, `sector_id`, `fila`, `numero`, `estado` (LIBRE, RESERVADO, VENDIDO), `reserva_expira_en`, `reservado_por_usuario_id`.
- **`compras`**: `id`, `usuario_id`, `evento_id`, `monto_total`, `medio_pago`, `estado` (PENDIENTE, APROBADA, DEVUELTA), `fecha_creacion`.
- **`entradas`**: `id`, `compra_id`, `asiento_id`, `codigo_qr_token`, `estado` (EMITIDA, USADA, ANULADA).
- **`devoluciones`**: `id`, `entrada_id`, `solicitante_id`, `fecha_solicitud`, `estado` (SOLICITADA, APROBADA, RECHAZADA).

---

## 4. Flujo de Compra y Manejo de Fallos (Secuencia)

1. **Cliente $\rightarrow$ Gateway $\rightarrow$ `ventas-service`:** Solicita reserva temporal (asiento ID + Evento ID).
2. **`ventas-service`:** Aplica bloqueo pesimista. Si está libre, genera orden en estado `PENDIENTE` con TTL de 10 min.
3. **Cliente $\rightarrow$ Gateway $\rightarrow$ `ventas-service`:** Confirma pago enviando datos del medio de pago.
4. **`ventas-service` $\rightarrow$ Mock Pagos:**
   - **Caso Aprobado:** Se confirma la compra, se generan los códigos QR únicos de las entradas y se pasa el estado del asiento a `VENDIDO`.
   - **Caso Rechazado:** Se cancela la orden y se libera inmediatamente el asiento a estado `LIBRE`.
   - **Caso Timeout:** Tarea programada (`@Scheduled`) o consulta posterior que detecta expiración de los 10 minutos revierte el asiento a `LIBRE`.

---

## 5. Diseño Preliminar de API REST

- `GET /api/eventos` - Búsqueda filtrada (query params: `tipo`, `ciudad`, `fechaDesde`, `fechaHasta`).
- `POST /api/eventos` - Creación de evento en borrador.
- `PUT /api/eventos/{id}/publicar` - Publicación de evento.
- `POST /api/ventas/reservas` - Reserva temporal de asientos (bloqueo concurrente).
- `POST /api/ventas/compras/confirmar` - Confirmación de compra y pago (emisión QR).
- `POST /api/ventas/entradas/{id}/devolucion` - Solicitud de reintegro (< 72 hs).