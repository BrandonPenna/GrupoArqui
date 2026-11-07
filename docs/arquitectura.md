# TicketYa - Documentación de Diseño y Arquitectura (Entrega 1)

## 1. Diagrama de Módulos de la Arquitectura de Solución
Representa los componentes del sistema, puertos, límites de microservicios y persistencia relacional aislada.

```mermaid
graph TD
    Client([Cliente / Web / Postman]) -->|HTTP :8080| Gateway[API Gateway :8080]

    subgraph "Red Interna de Microservicios"
        Gateway -->|/api/eventos/**, /api/lugares/**| CatService[catalogo-service :8081]
        Gateway -->|/api/reservas/**, /api/compras/**| VenService[ventas-service :8082]
    end

    subgraph "Persistencia (Bases Relacionales Independientes)"
        CatService --- DBCat[(H2: db_catalogo)]
        VenService --- DBVen[(H2: db_ventas)]
    end

    subgraph "Servicios Externos Simulados"
        VenService -->|Mock| Pasarela[Pasarela de Pagos Externa]
    end