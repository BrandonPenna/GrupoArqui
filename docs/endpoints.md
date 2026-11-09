# TicketYa - Listado de Endpoints (Entrega 1)

Todas las rutas `/api/**` se llaman a través del **API Gateway** (`http://localhost:8080`).
La documentación OpenAPI completa está en **http://localhost:8080/swagger-ui.html**, donde se elige `catalogo-service` o
`ventas-service` arriba a la derecha. También está en cada servicio: `http://localhost:8081/swagger-ui.html` y
`http://localhost:8082/swagger-ui.html`. El JSON OpenAPI está en `/v3/api-docs`.

En la Entrega 1 no hay autenticación: el usuario se identifica con los encabezados `X-User-Id` y `X-User-Role`
(`ADMIN`, `ORGANIZADOR`, `CLIENTE`).

Los errores se devuelven en formato **ProblemDetail** (RFC 9457):

```json
{ "title": "Conflict", "status": 409, "detail": "El lugar elegido acaba de ser reservado por otro cliente", "instance": "/api/reservas" }
```

---

## catalogo-service (puerto 8081)

| Método | Ruta | Parámetros | Respuestas |
|---|---|---|---|
| POST | `/api/lugares` | Header `X-User-Role` (ADMIN u ORGANIZADOR). Body `LugarDTO` | 201, 403 |
| GET | `/api/lugares` | - | 200 |
| GET | `/api/lugares/{id}` | Path `id` | 200, 404 |
| POST | `/api/eventos` | Headers `X-User-Id`, `X-User-Role` (ORGANIZADOR). Body `EventoCreateDTO` | 201, 403, 404 |
| PUT | `/api/eventos/{id}/publicar` | Path `id`. Headers `X-User-Id`, `X-User-Role` (ORGANIZADOR dueño) | 200, 403, 404, 409 |
| PUT | `/api/eventos/{id}/suspender` | Path `id`. Header `X-User-Role` (ADMIN) | 200, 403, 404, 409 |
| PUT | `/api/eventos/{id}/reactivar` | Path `id`. Header `X-User-Role` (ADMIN) | 200, 403, 404, 409 |
| PUT | `/api/eventos/{id}/cancelar` | Path `id`. Headers `X-User-Id`, `X-User-Role` (ADMIN u ORGANIZADOR dueño) | 200, 403, 404, 409, 503 |
| GET | `/api/eventos` | Query: `q`, `tipo`, `organizadorId`, `participante`, `ciudad`, `fechaDesde`, `fechaHasta`, `orden=relevancia`, `page`, `size`, `sort` | 200 |
| GET | `/api/eventos/{id}` | Path `id` | 200, 404 |

### Crear lugar - `POST /api/lugares`

Request:
```json
{
  "nombre": "Teatro de Prueba",
  "direccion": "Calle 123",
  "ciudad": "Tandil",
  "latitud": -37.32,
  "longitud": -59.13,
  "sectores": [
    { "nombre": "Platea", "capacidad": 100, "esNumerado": true, "filas": 10, "asientosPorFila": 10 }
  ]
}
```
Response `201`:
```json
{
  "id": 1, "nombre": "Teatro de Prueba", "direccion": "Calle 123", "ciudad": "Tandil",
  "latitud": -37.32, "longitud": -59.13,
  "sectores": [ { "id": 1, "nombre": "Platea", "capacidad": 100, "esNumerado": true, "filas": 10, "asientosPorFila": 10 } ]
}
```

### Crear evento - `POST /api/eventos`

Request (header `X-User-Id: org-1`):
```json
{
  "nombre": "Recital de Prueba",
  "descripcion": "Evento para probar la API",
  "tipo": "RECITAL",
  "participantes": ["Banda A"],
  "fechaHora": "2026-12-15T20:00:00",
  "lugarId": 1,
  "precios": [ { "sectorId": 1, "precio": 15000 } ]
}
```
Response `201`:
```json
{
  "id": 1, "nombre": "Recital de Prueba", "descripcion": "Evento para probar la API", "tipo": "RECITAL",
  "organizadorId": "org-1", "participantes": ["Banda A"], "fechaHora": "2026-12-15T20:00:00",
  "lugar": { "id": 1, "nombre": "Teatro de Prueba", "ciudad": "Tandil", "...": "..." },
  "estado": "BORRADOR", "precioDesde": 15000,
  "preciosSectores": [ { "id": 1, "sector": { "id": 1, "nombre": "Platea", "...": "..." }, "precio": 15000 } ]
}
```
Error `404`: `{ "title": "Not Found", "status": 404, "detail": "Lugar no encontrado con ID: 99" }`

### Publicar, suspender, reactivar y cancelar

No llevan body. Responden `200` con el evento y su nuevo `estado` (`PUBLICADO`, `SUSPENDIDO`, `PUBLICADO` o `CANCELADO`).
Ejemplo de error `409` al suspender un borrador:
```json
{ "title": "Conflict", "status": 409, "detail": "Solo se puede suspender un evento publicado o agotado" }
```

### Buscar eventos - `GET /api/eventos`

Ejemplos:
- `/api/eventos?ciudad=Tandil&tipo=RECITAL&sort=fechaHora,asc`
- `/api/eventos?ciudad=Tandil&sort=precioDesde,asc`
- `/api/eventos?q=piojos&orden=relevancia`

Response `200` (página de Spring Data):
```json
{
  "content": [ { "id": 1, "nombre": "Recital de Prueba", "estado": "PUBLICADO", "precioDesde": 15000, "...": "..." } ],
  "number": 0, "size": 10, "totalElements": 1, "totalPages": 1, "first": true, "last": true
}
```

---

## ventas-service (puerto 8082)

| Método | Ruta | Parámetros | Respuestas |
|---|---|---|---|
| POST | `/api/reservas` | Header `X-User-Id`. Body `ReservaCreateDTO` | 201, 400, 409 |
| GET | `/api/reservas` | Header `X-User-Id` | 200 |
| POST | `/api/compras/confirmar` | Header `X-User-Id`. Body `ConfirmarCompraDTO` | 200, 402, 403, 404, 409 |
| GET | `/api/compras` | Header `X-User-Id` | 200 |
| POST | `/api/devoluciones/entradas/{entradaId}` | Path `entradaId`. Header `X-User-Id`. Body `SolicitudDevolucionDTO` | 201, 403, 404, 409 |
| PUT | `/api/devoluciones/{devolucionId}/resolver` | Path `devolucionId`. Header `X-User-Role` (ADMIN). Body `ResolverDevolucionDTO` | 200, 400, 403, 404, 409 |
| POST | `/internal/eventos/{eventoId}/cancelacion` | Path `eventoId`. **Solo entre servicios, el gateway no la expone** | 200 |

### Crear reserva - `POST /api/reservas`

Request (header `X-User-Id: cliente-1`):
```json
{
  "eventoId": 1,
  "sectorId": 1,
  "esNumerado": true,
  "asientos": [ { "fila": 2, "asiento": 3, "precio": 15000 } ]
}
```
Para un sector no numerado se manda `"esNumerado": false`, `"capacidadMaximaSector": 500` y un elemento en `asientos`
por cada entrada, sin fila ni asiento.

Response `201`:
```json
{
  "id": 1, "clienteId": "cliente-1", "eventoId": 1,
  "fechaCreacion": "2026-11-09T22:19:32", "fechaExpiracion": "2026-11-09T22:29:32",
  "estado": "PENDIENTE", "fechaEvento": "2026-12-15T20:00:00",
  "entradas": [ { "id": 1, "eventoId": 1, "sectorId": 1, "fila": 2, "asiento": 3, "precio": 15000, "codigoQR": null, "estado": "RESERVADA" } ]
}
```
Error `409` si otro cliente ya tomó el asiento:
```json
{ "title": "Conflict", "status": 409, "detail": "El lugar elegido acaba de ser reservado por otro cliente", "instance": "/api/reservas" }
```

### Confirmar compra - `POST /api/compras/confirmar`

Request (`"medioPago": "RECHAZAR"` simula un pago rechazado):
```json
{ "reservaId": 1, "medioPago": "TARJETA_CREDITO" }
```
Response `200`:
```json
{
  "id": 1, "reservaId": 1, "clienteId": "cliente-1", "eventoId": 1, "montoTotal": 15000.00,
  "fechaCompra": "2026-11-09T22:19:32", "medioPago": "TARJETA_CREDITO", "transaccionExternaId": "PAY-6733B28D",
  "entradas": [ { "id": 1, "fila": 2, "asiento": 3, "precio": 15000.00, "codigoQR": "QR-b8b2ecf3-8f74-4c1b-be6b-363be6f4ce6c", "estado": "VENDIDA" } ]
}
```
Error `402`: `{ "title": "Payment Required", "status": 402, "detail": "Pago rechazado por la pasarela. Se liberaron los asientos." }`

### Solicitar devolución - `POST /api/devoluciones/entradas/{entradaId}`

Request:
```json
{ "motivo": "No puedo asistir" }
```
Response `201`:
```json
{
  "id": 1, "clienteId": "cliente-1", "motivo": "No puedo asistir",
  "fechaSolicitud": "2026-11-09T22:19:32", "fechaResolucion": null, "transaccionReintegroId": null,
  "estado": "SOLICITADA", "entrada": { "id": 1, "estado": "VENDIDA", "...": "..." }
}
```

### Resolver devolución - `PUT /api/devoluciones/{devolucionId}/resolver`

Request (header `X-User-Role: ADMIN`):
```json
{ "aprobar": true }
```
Response `200`: la devolución con `"estado": "APROBADA"`, `"transaccionReintegroId": "REF-F0A225C8"` y la entrada en `DEVUELTA`.

### Cancelación interna - `POST /internal/eventos/{eventoId}/cancelacion`

La llama `catalogo-service` al cancelar un evento. Response `200`:
```json
{ "eventoId": 1, "reservasCanceladas": 1, "entradasReintegradas": 3, "clientesNotificados": 2 }
```
