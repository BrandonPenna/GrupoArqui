# Tasks: Core del Sistema TicketYa (Entrega 1)

## 1. Infraestructura y Setup Base
- [ ] 1.1 Configurar `docker-compose.yml` local para PostgreSQL con bases `db_catalogo` y `db_ventas`.
- [ ] 1.2 Crear el proyecto `api-gateway` con Spring Cloud Gateway configurando enrutamiento a puertos 8081 y 8082.
- [ ] 1.3 Inicializar los proyectos `catalogo-service` y `ventas-service` en Spring Boot con dependencias JPA y Web.

## 2. Microservicio de Catálogo (`catalogo-service`)
- [ ] 2.1 Implementar entidades JPA: `Lugar`, `Sector` (numerado y no numerado), `Evento` y `PrecioSector`.
- [ ] 2.2 Implementar repositorio `EventoRepository` con `JpaSpecificationExecutor`.
- [ ] 2.3 Crear servicio para publicación de eventos y cambio de estados (Borrador -> Publicado).
- [ ] 2.4 Exponer controladores REST con documentación Swagger/OpenAPI para CRUD y búsqueda multicriterio.

## 3. Microservicio de Ventas y Reservas (`ventas-service`)
- [ ] 3.1 Implementar entidades JPA: `AsientoInventario`, `Compra`, `Entrada` y `Devolucion`.
- [ ] 3.2 Desarrollar servicio de reserva temporal (10 min) aplicando `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
- [ ] 3.3 Implementar mock de pasarela de pagos simulada con escenarios de éxito y rechazo.
- [ ] 3.4 Implementar confirmación de compra y generación de token único QR para cada entrada emitida.
- [ ] 3.5 Implementar servicio de devoluciones con validación de ventana temporal (hasta 72 hs previas al evento).
- [ ] 3.6 Implementar cancelación masiva de entradas por cancelación de evento.

## 4. Testing y Verificación de Concurrencia
- [ ] 4.1 Configurar pool de HikariCP en test para soportar múltiples hilos concurrentes.
- [ ] 4.2 Desarrollar test de integración multihilo que verifique que ante 20+ intentos simultáneos solo 1 reserva el asiento.
- [ ] 4.3 Desarrollar pruebas unitarias de lógica de negocio (búsqueda, expiración de reserva, devoluciones).

## 5. Documentación y Entrega
- [ ] 5.1 Exportar especificación OpenAPI generada por Swagger.
- [ ] 5.2 Generar colección de Postman con datos de prueba (*seed data*).
- [ ] 5.3 Redactar README con instrucciones paso a paso para levantar las bases y ejecutar los microservicios.