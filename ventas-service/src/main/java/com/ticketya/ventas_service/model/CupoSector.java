package com.ticketya.ventas_service.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Contador de lugares ocupados de un sector NO numerado (ej. "Campo") para un evento.
 * Hay una sola fila por (evento, sector). Al reservar se bloquea esa fila con
 * SELECT ... FOR UPDATE, así las compras concurrentes del "último lugar" se atienden
 * de a una aunque haya varias instancias de ventas-service.
 */
@Entity
@Table(name = "cupos_sector",
        uniqueConstraints = @UniqueConstraint(name = "uk_cupo_evento_sector", columnNames = {"evento_id", "sector_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CupoSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "sector_id", nullable = false)
    private Long sectorId;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    @Builder.Default
    private Integer ocupados = 0;

    public int disponibles() {
        return capacidad - ocupados;
    }
}
