package com.afcrm.server.model;

import lombok.Builder;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "informes_servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private LocalTime hora;
    private String tipo;
    private String adjuntos;
    private String observaciones;

    @Column(name = "tipo_prueba")
    private String tipoPrueba;

    private Boolean completar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_task_id")
    private ScheduledTask scheduledTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Renamed from tecnicos_id for clarity
    private User user;

    @ManyToMany
    @JoinTable(
        name = "informes_servicios_partes",
        joinColumns = @JoinColumn(name = "informes_servicios_id"),
        inverseJoinColumns = @JoinColumn(name = "partes_id")
    )
    private Set<TaskPart> taskParts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> measurements;
}
