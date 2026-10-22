package com.afcrm.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "direccion_fisica", nullable = false)
    private String direccionFisica;

    private String localidad;

    private String provincia;

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia_visita", nullable = false)
    @Builder.Default
    private ServiceFrequency frecuenciaVisita = ServiceFrequency.MENSUAL;

    @Column(name = "dias_aviso_alerta", nullable = false)
    @Builder.Default
    private Integer diasAvisoAlerta = 7;

    @Column(name = "fecha_ultima_visita")
    private LocalDate fechaUltimaVisita;

    @Column(name = "fecha_proxima_visita_limite")
    private LocalDate fechaProximaVisitaLimite;

    @Column(columnDefinition = "text")
    private String notas;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Equipment> equipments = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkOrder> workOrders = new ArrayList<>();
}

