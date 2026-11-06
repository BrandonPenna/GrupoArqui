# Propuesta: Core del Sistema TicketYa (Entrega 1)

## 1. Contexto y Problema
TicketYa requiere una plataforma escalable para la publicación de eventos y venta de entradas. El núcleo del negocio exige soportar alta concurrencia evitando la sobreventa de localidades (doble venta), permitiendo reservas temporales y garantizando la consistencia en la compra sin autenticación formal en esta primera etapa.

## 2. Alcance de la Entrega 1
Esta propuesta cubre el núcleo transaccional y de catálogo (Funcionalidades 1, 2, 3, 4, 5, 7 y 9 de la consigna):
- **Gestión de Lugares y Eventos:** Creación de lugares con sectores numerados y no numerados (campo). Creación de eventos en estado borrador y posterior publicación.
- **Búsqueda multicriterio:** Filtrado por tipo de evento, organizador, participante, ciudad y fechas.
- **Circuito de Reserva y Compra:** Reserva temporal de ubicaciones por 10 minutos con bloqueo contra doble venta. Emisión de entrada con código QR único ante pago aprobado vía mock.
- **Liberación y Devoluciones:** Liberación automática por timeout de reserva o rechazo de pago. Cancelación administrativa de eventos y solicitud de devolución por parte del cliente (hasta 72 hs previas).

## 3. Arquitectura Propuesta
- **Punto de Entrada Único:** Spring Cloud Gateway para enrutamiento.
- **Microservicios:**
  - `catalogo-service`: Gestión y consulta de lugares, sectores y eventos (Base de datos relacional).
  - `ventas-service`: Gestión de reservas, asientos, órdenes, pasarela simulada (mock) y entradas (Base de datos relacional).
- **Concurrencia:** Bloqueo pesimista (`Pessimistic Lock`) a nivel base de datos sobre los asientos y cupos de sectores no numerados durante la reserva.
- **Identificación:** Usuario simulado vía headers HTTP (`X-User-Id`, `X-User-Role`).

## 4. Criterios de Éxito
- Cero doble venta verificado bajo prueba multihilo automatizada con solicitudes concurrentes al mismo asiento/cupo.
- Expiración consistente de reservas temporales liberando los lugares para otros compradores.
- Microservicios independientes ejecutables localmente con persistencia relacional en contenedores Docker.