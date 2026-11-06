# Spec Delta

## Purpose

Proporciona la gestión del catálogo de eventos y el circuito transaccional de reserva temporal con prevención de sobreventa y devoluciones para TicketYa.

## ADDED Requirements

### Requirement: Gestión de Lugares y Eventos
El sistema SHALL permitir a los organizadores crear lugares con sectores numerados y no numerados, crear eventos en borrador y publicarlos posteriormente.

#### Scenario: Publicación exitosa de un evento
- **WHEN** un organizador publica un evento que tiene sectores y precios configurados
- **THEN** el sistema MUST cambiar el estado del evento a PUBLICADO y dejarlo disponible para la venta

### Requirement: Búsqueda Multicriterio de Eventos
El sistema SHALL permitir a los usuarios buscar eventos publicados por tipo, organizador, participante, ciudad y rango de fechas.

#### Scenario: La búsqueda retorna eventos coincidentes
- **WHEN** un usuario consulta eventos filtrando por ciudad y rango de fechas
- **THEN** el sistema MUST retornar únicamente los eventos publicados que coincidan con todos los criterios

### Requirement: Reserva Temporal con Control de Concurrencia
El sistema SHALL bloquear y reservar los asientos o cupos seleccionados por exactamente 10 minutos, impidiendo la doble venta ante solicitudes simultáneas.

#### Scenario: Solo una reserva resulta exitosa para el mismo asiento
- **WHEN** múltiples peticiones concurrentes intentan reservar exactamente el mismo asiento al mismo tiempo
- **THEN** el sistema MUST conceder la reserva a exactamente una petición y rechazar todas las demás

#### Scenario: Liberación de reserva expirada
- **WHEN** transcurre la ventana de 10 minutos de una reserva sin un pago confirmado
- **THEN** el sistema MUST liberar automáticamente el asiento dejándolo disponible para otros compradores

### Requirement: Confirmación de Compra y Emisión de QR
El sistema SHALL confirmar las compras cuando la pasarela de pagos autorice el cobro y generar un código QR único por entrada emitida.

#### Scenario: Compra exitosa genera entrada con QR único
- **WHEN** el pago es aprobado por la pasarela para una reserva activa
- **THEN** el sistema MUST emitir la entrada con un token QR único y registrar el detalle de la compra

### Requirement: Gestión de Devoluciones y Cancelaciones
El sistema SHALL permitir a los clientes solicitar devoluciones hasta 72 horas antes del evento, y a los administradores cancelar eventos emitiendo reembolsos totales.

#### Scenario: Devolución solicitada con más de 72 horas de anticipación
- **WHEN** un cliente solicita la devolución de una entrada 80 horas antes del inicio del evento
- **THEN** el sistema MUST aceptar la solicitud para revisión administrativa y liberar el lugar una vez aprobada