# TicketYa - Plataforma de Venta de Entradas (TUDAI - UNICEN)

**Trabajo Práctico de Arquitectura Web (2026) - Entrega 1 (Core del Sistema)**

---

## Arquitectura del Sistema

El sistema está diseñado bajo una arquitectura de microservicios con Spring Boot 3, API Gateway y persistencia relacional independiente por servicio (H2 file-based).

```mermaid
graph TD
    Client["Cliente / Web / Postman"] -->|HTTP :8080| Gateway["API Gateway (puerto 8080)"]

    subgraph RedInterna ["Red Interna de Microservicios"]
        Gateway -->|/api/eventos/**| CatService["catalogo-service (puerto 8081)"]
        Gateway -->|/api/reservas/**| VenService["ventas-service (puerto 8082)"]
    end

    subgraph BasesDatos ["Persistencia H2 Embebida"]
        CatService --- DBCat["(db_catalogo)"]
        VenService --- DBVen["(db_ventas)"]
    end

    subgraph Externos ["Servicios Externos Simulados"]
        VenService -->|Mock| Pasarela["pasarela de pagos Mock"]
    end
```

---

## Microservicios y Puertos

| Microservicio | Puerto | Responsabilidad Principal |
|---|---|---|
| **api-gateway** | `8080` | Punto de entrada único, ruteo y validación de headers |
| **catalogo-service** | `8081` | Lugares, Sectores, Eventos y búsqueda multicriterio |
| **ventas-service** | `8082` | Reservas temporales (10 min), concurrencia, pagos y QR |

---

## Instrucciones de Ejecución

### Requisitos
* Java 17 o superior.
* Git.

### Levantar los servicios (en terminales separadas)

1. **Catalogo Service:**
```powershell
cd catalogo-service
.\mvnw spring-boot:run
```

2. **Ventas Service:**
```powershell
cd ventas-service
.\mvnw spring-boot:run
```

3. **API Gateway:**
```powershell
cd api-gateway
.\mvnw spring-boot:run
```

---

## Documentación Técnica Adicional

Los diagramas detallados exigidos por la cùtedra se encuentran en el directorio [docs/arquitectura.md](docs/arquitectura.md):
* **Diagrama de Secuencia:** Flujo completo de compra, reserva, rechazo y expiración por timeout.
* **Diagrama Entidad-Relación (DER):** Esquema relacional y referencias lógicas entre microservicios.
* **Features y User Stories:** [docs/user-stories.md](docs/user-stories.md), con criterios de aceptación.
