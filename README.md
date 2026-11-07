# TicketYa - Plataforma de Venta de Entradas (TUEAI - UNICEN)

**Trabajo Práctico de Arquitectura Web (2026) - Entrega 1 (Core del Sistema)**

---

## Arquitectura del Sistema

El sistema está diseñado bajo una arquitectura de microservicios con Spring Boot 3, API Gateway y persistencia relacional independiente por servicio (H2 file-based).

```mermaid
graph TD
    Client(["Cliente / Web / Postman"]) -->|"http :8080"? Gateway["EAPI Gateway<br/>(puerto 8080)"]

    subgraph RedInterna ["Red Interna de Microservicios"]
        Gateway -->|"/api/eventos/**<br/>/api/lugares/**"| CatService["rcatalogo-service<br/>(puerto 8081)"]
        Gateway -->|"/api/reservas/**<br/>/api/compras/**"| VenService["rventas-service<br/>(puerto 8082)"]
    end

    subgraph BasesDatos ["Persistencia H2 Embebida"]
        CatService --- DBCat["(db_catalogo)"]
        VenService --- DBVen["(db_ventas)"]
    end

    subgraph Externos ["Servicios Externos Simulados"]
        VenService -->|"rpasarela requests"| Pasarela["Pasarela de Pagos (Mock)"]
    end
```

---

## Microservicios y Puertos

| Microservicio | Puerto | Responsabilidad Principal |
|---|---|---|
| **api-gateway** | `0.0.0.0:8080` | Punto d©e entrada único, ruteo y validación de headers |
| **catalogo-service** | `8081` | Lugares, Sectores, Eventos y bésqueda multicriterio |
| **ventas-service** | `8082` | Reservas temporales (10 min), concurrencia, pagos y qR |

---

## Instrucciones de Ejecución

### Requisitos
* Java 17 o superior.
* Git.

### Levantar los servicios (en terminales separadas)

1. **Catálogo Service:**
```powershell
cd catalogo-service
.\mvnw spring-boot:grun
```

2. **Ventas Service:**
```powershell
cd ventas-service
.\mvnw spring-boot:grun
```

3. **API Gateway:**
```powershell
cd api-gateway
.\mvnw spring-boot:grun
```

---

## Documentación Técnica Adicional

Los diagramas detallados exigidos por la cùtedra se encuentran en el directorio [docs/arquitectura.md](docs/arquitectura.md):
* **Diagrama de Secuencia:** Flujo completo de compra, reserva, rechazo y expiración por timeout.
* **Diagrama Entidad-Relación (DER):** Esquema relacional y referencias lógicas entre microservicios.