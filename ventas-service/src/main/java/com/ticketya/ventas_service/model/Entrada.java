package com.ticketya.ventas_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "entradas",
        uniqueConstraints = @UniqueConstraint(name = "uk_entrada_asiento_activo", columnNames = "clave_asiento"))
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

    /**
     * Mecanismo contra la doble venta en sectores numerados.
     * Mientras la entrada está RESERVADA o VENDIDA vale "evento-sector-fila-asiento";
     * cuando el lugar se libera (pago rechazado, reserva vencida, devolución) se pone en null.
     * La restricción UNIQUE hace que, si dos pedidos toman el mismo asiento a la vez,
     * solo uno pueda insertar la fila; el otro recibe 409.
     * (Varios NULL no violan un UNIQUE, así que las entradas liberadas no molestan.)
     */
    @Column(name = "clave_asiento", length = 100)
    @JsonIgnore
    private String claveAsiento;

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