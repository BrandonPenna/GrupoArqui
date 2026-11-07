package com.ticketya.catalogo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo;

    @Column(nullable = false)
    private String organizadorId;

    @ElementCollection
    @CollectionTable(name = "evento_participantes", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(name = "participante")
    @Builder.Default
    private List<String> participantes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lugar_id", nullable = false)
    private Lugar lugar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventoPrecioSector> preciosSectores = new ArrayList<>();
}