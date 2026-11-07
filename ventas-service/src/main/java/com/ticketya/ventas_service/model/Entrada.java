package com.ticketya.ventas_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "entradas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventoId;

    @Column(nullable = false)
    private Long sectorId;

    // Null si el sector es no numerado (ej. Campo)
    private Integer fila;

    // Null si el sector es no numerado (ej. Campo)
    private Integer asiento;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(unique = true)
    private String codigoQR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEntrada estado = EstadoEntrada.RESERVADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    @JsonIgnore
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    @JsonIgnore
    private Compra compra;
}