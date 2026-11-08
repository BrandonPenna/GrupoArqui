package com.ticketya.ventas_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entrada_id", nullable = false)
    private Entrada entrada;

    @Column(nullable = false)
    private String clienteId;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaResolucion;

    private String transaccionReintegroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoDevolucion estado = EstadoDevolucion.SOLICITADA;
}