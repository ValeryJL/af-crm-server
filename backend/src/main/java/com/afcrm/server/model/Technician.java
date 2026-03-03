package com.afcrm.server.model;

import lombok.Builder;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tecnicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String apellido;

    private String mail;

    private String telefono;
}
