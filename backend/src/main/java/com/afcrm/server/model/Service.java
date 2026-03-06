package com.afcrm.server.model;

import lombok.Builder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private String tipo;
    @Enumerated(EnumType.STRING)
    private ServiceFrequency frecuencia;
    private String observaciones;
    private String planilla;
    private String cliente;
    private String contactos;
    @Column(name = "requerimientos_especificos", length = 1000)
    private String requerimientos;

    private LocalDate alta;
    private LocalDate baja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupos_id")
    private Group group;

    @ManyToMany
    @JoinTable(
        name = "partes_servicios",
        joinColumns = @JoinColumn(name = "servicios_id"),
        inverseJoinColumns = @JoinColumn(name = "partes_id")
    )
    private Set<TaskPart> taskParts;
}
