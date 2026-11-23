# Tasks: Core del Sistema TicketYa (Entrega 1)

## 1. Infraestructura y Setup Base
- [x] 1.1 Configurar persistencia relacional independiente por servicio (`db_catalogo` y `db_ventas` con H2 file-based).
- [x] 1.2 Crear el proyecto `api-gateway` con Spring Cloud Gateway configurando enrutamiento a puertos 8081 y 8082.
- [x] 1.3 Inicializar los proyectos `catalogo-service` y `ventas-service` en Spring Boot con dependencias JPA y Web.

## 2. Microservicio de Catálogo (`catalogo-service`)
- [x] 2.1 Implementar entidades JPA: `Lugar`, `Sector` (numerado y no numerado), `Evento` y `EventoPrecioSector`.
- [x] 2.2 Implementar repositorio `EventoRepository` con `JpaSpecificationExecutor`.
- [x] 2.3 Crear servicio para publicación, suspensión, reactivación y cancelación de eventos.
- [x] 2.4 Exponer controladores REST con documentación Swagger/OpenAPI para CRUD y búsqueda multicriterio (fecha, precio, relevancia).

## 3. Microservicio de Ventas y Reservas (`ventas-service`)
- [x] 3.1 Implementar entidades JPA: `Reserva`, `Compra`, `Entrada`, `Devolucion` y `CupoSector`.
- [x] 3.2 Desarrollar servicio de reserva temporal (10 min) con restricción `UNIQUE` en asientos numerados y `@Lock(LockModeType.PESSIMISTIC_WRITE)` en cupos de sectores generales.
- [x] 3.3 Implementar mock de pasarela de pagos simulada con escenarios de éxito, rechazo y reintegro.
- [x] 3.4 Implementar confirmación de compra idempotente y generación de token único QR para cada entrada emitida.
- [x] 3.5 Implementar servicio de devoluciones con validación de ventana temporal (hasta 72 hs previas al evento).
- [x] 3.6 Implementar cancelación masiva de entradas y reintegros por cancelación de evento.

## 4. Testing y Verificación de Concurrencia
- [x] 4.1 Configurar entorno de test en H2 para soportar múltiples hilos concurrentes.
- [x] 4.2 Desarrollar test de integración multihilo (`DobleVentaConcurrenteTest`) que verifique que ante 50 intentos simultáneos solo 1 reserva el asiento o último lugar de zona.
- [x] 4.3 Desarrollar pruebas unitarias de lógica de negocio (búsqueda, compra, idempotencia de pago, devoluciones, cancelación y suspensión).

## 5. Documentación y Entrega
- [x] 5.1 Exponer especificación OpenAPI generada por Swagger unificada en el API Gateway.
- [x] 5.2 Generar colección de Postman con datos de prueba (*seed data*) en `docs/TicketYa-Entrega1.postman_collection.json`.
- [x] 5.3 Redactar README y documentación de diseño (`docs/arquitectura.md`, `docs/user-stories.md`, `docs/endpoints.md`).


---

# Tasks: Sistema Completo TicketYa (Entrega 2)

## 6. Funcionalidad Completa (Entrega 2)
- [x] 6.1 Implementar Fila Virtual por orden de llegada y tandas para eventos de alta demanda (Req. 6 / US-17).
- [x] 6.2 Implementar Control de Acceso con validación y bloqueo pesimista de código QR en el ingreso (Req. 8 / US-18).
- [x] 6.3 Implementar modelo documental NoSQL para Preferencias de Clientes y motor de Recomendaciones por afinidad e historial co-comprado (Req. 10 / US-19 y US-20).
- [x] 6.4 Implementar servicio de Notificaciones con resiliencia ante confirmación de compra, cancelación y publicación de artista favorito en la ciudad del cliente (Req. 11 / US-21).
- [x] 6.5 Implementar Reportes para Organizadores con entradas vendidas, utilizadas, devueltas, porcentaje de ocupación y recaudación por sector (Req. 12 / US-22).

## 7. Arquitectura, Seguridad, Despliegue e Interfaz Gráfica (Entrega 2)
- [x] 7.1 Incorporar autenticación y autorización con tokens JWT (HS256) y roles (`CLIENTE`, `ORGANIZADOR`, `ADMIN`, `PERSONAL_ACCESO`) (US-23).
- [x] 7.2 Implementar estrategia de Cache con TTL e invalidación (`@Cacheable` / `@CacheEvict`) en `catalogo-service`.
- [x] 7.3 Implementar Resiliencia (reintentos, timeouts y fallback) en `PasarelaPagosMockService` y `NotificacionService`.
- [x] 7.4 Implementar Trazabilidad HTTP y bitácora centralizada con propagación de `X-Correlation-Id`.
- [x] 7.5 Desarrollar Interfaz Gráfica Web interactiva en el API Gateway (`http://localhost:8080/`).
- [x] 7.6 Empaquetar servicios con `Dockerfile` y orquestar con `docker-compose.yml`.
- [x] 7.7 Desarrollar pruebas de integración, seguridad por roles e informe de prueba de carga (`docs/arquitectura.md` y `docs/ticketya-compra-load-test.jmx`).
