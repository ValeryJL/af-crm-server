package com.afcrm.server.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;

    private String modelo;

    private String serie;

    private String motor;

    private String generador;

    private String potencia;
}
