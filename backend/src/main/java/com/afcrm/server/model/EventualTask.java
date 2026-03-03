package com.afcrm.server.model;

import lombok.Builder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "eventuales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventualTask extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private LocalDate fecha;
    private String adjuntos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicios_id")
    private Service service;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "informes_servicios_id")
    private ServiceReport serviceReport;
}
