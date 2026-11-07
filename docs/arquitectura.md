# TicketYa - Documentación de Diseño y Arquitectura (Entrega 1)

## 1. Diagrama de Módulos de la Arquitectura de Solución

```mermaid
graph TD
    Client([Cliente / Web / Postman]) -->|HTTP :8080| Gateway[API Gateway :8080]

    subgraph "Red Interna de Microservicios"
        Gateway -->|/api/eventos/**, /api/lugares/**| CatService[catalogo-service :8081]
        Gateway -->|/api/reservas/**, /api/compras/**| VenService[ventas-service :8082]
    end

    subgraph "Persistencia H2 Embebida"
        CatService --- DBCat[(db_catalogo)]
        VenService --- DBVen[(db_ventas)]
    end

    subgraph "Servicios Externos Simulados"
        VenService -->|Mock| Pasarela[Pasarela de Pagos]
    end
```

---

## 2. Diagrama de Secuencia: Flujo de Compra de Entrada Numerada

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Gateway as API Gateway :8080
    participant Ventas as Ventas Service :8082
    participant DB as DB Ventas H2
    participant Pagos as Pasarela de Pagos Mock

    Cliente->>Gateway: POST /api/reservas
    Gateway->>Ventas: POST /api/reservas
    
    activate Ventas
    Note over Ventas,DB: Bloqueo Pesimista PESSIMISTIC_WRITE
    Ventas->>DB: Consultar y bloquear asiento
    
    alt Asiento ocupado
        Ventas-->>Cliente: 409 Conflict
    else Asiento libre
        Ventas->>DB: Crear Reserva PENDIENTE (vence: 10 min)
        Ventas-->>Cliente: 201 Created (reservaId, expiraEn: 10 min)
    end
    deactivate Ventas

    alt Flujo A: El cliente paga a tiempo
        Cliente->>Gateway: POST /api/compras/confirmar (reservaId, datosPago)
        Gateway->>Ventas: Confirmar compra
        activate Ventas
        Ventas->>Pagos: Procesar cargo
        alt Pago Aprobado
            Pagos-->>Ventas: OK
            Ventas->>DB: Marcar Entrada VENDIDA + Generar Hash/QR unico
            Ventas-->>Cliente: 200 OK (Entrada con QR)
        else Pago Rechazado
            Pagos-->>Ventas: Error de cobro
            Ventas->>DB: Liberar Asiento (Reserva CANCELADA)
            Ventas-->>Cliente: 402 Payment Required
        end
        deactivate Ventas

    else Flujo B: La reserva expira (Pasan los 10 min sin pagar)
        Note over Ventas,DB: Tarea programada @Scheduled cada 1 min
        Ventas->>DB: Liberar asientos con tiempo vencido (EXPIRADA)
    end
```

---

## 3. Diagrama Entidad-Relación (DER) por Microservicio

```mermaid
erDiagram
    LUGARES ||--o{ SECTORES : tiene
    LUGARES ||--o{ EVENTOS : alberga
    EVENTOS ||--o{ EVENTO_PRECIOS_SECTORES : define
    SECTORES ||--o{ EVENTO_PRECIOS_SECTORES : tarifa

    LUGARES {
        Long id PK
        String nombre
        String direccion
        String ciudad
    }

    SECTORES {
        Long id PK
        String nombre
        Integer capacidad
        Boolean esNumerado
        Long lugar_id FK
    }

    EVENTOS {
        Plong id PK
        String nombre
        String tipo
        String organizadorId
        DateTime fechaHora
        String estado
        Long lugar_id FK
    }

    EVENTO_PRECIOS_SECTORES {
        Long id PK
        BigDecimal precio
        Long evento_id FK
        Long sector_id FK
    }

    RESERVAS ||--o{ ENTRADAS : contiene
    COMPRAS ||--o{ ENTRADAS : emite

    RESERVAS {
        Long id PK
        String clienteId
        Long eventoId
        DateTime fechaCreacion
        DateTime fechaExpiracion
        String estado
    }

    ENTRADAS {
        Long id PK
        Long sectorId
        Integer fila
        Integer asiento
        String codigoQR
        String estado
        Long reserva_id FK
        Long compra_id FK
    }

    COMPRAS {
        Long id PK
        String clienteId
        BigDecimal montoTotal
        DateTime fechaCompra
        String medioPago
        String transaccionExternaId
    }
```