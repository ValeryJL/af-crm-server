package com.afcrm.server.model;

import lombok.Builder;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "partes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskPart extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @Column(name = "duracion_dias")
    private Long duracionDias;

    @Column(name = "duracion_horas")
    private Long duracionHoras;

    @ManyToMany(mappedBy = "taskParts")
    private Set<Service> services;
}
