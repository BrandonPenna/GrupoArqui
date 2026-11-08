package com.ticketya.ventas_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras",
        uniqueConstraints = @UniqueConstraint(name = "uk_compra_reserva", columnNames = "reserva_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Una reserva genera como máximo una compra (UNIQUE): un reintento nunca crea una segunda compra. */
    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private Long eventoId;

    @Column(nullable = false)
    private BigDecimal montoTotal;

    @Column(nullable = false)
    private LocalDateTime fechaCompra;

    @Column(nullable = false)
    private String medioPago;

    @Column(nullable = false)
    private String transaccionExternaId;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Entrada> entradas = new ArrayList<>();
}