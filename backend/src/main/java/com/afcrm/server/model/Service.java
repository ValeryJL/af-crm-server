package com.afcrm.server.model;

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
    private String frecuencia;
    private String observaciones;
    private String planilla;
    private String cliente;

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
