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
