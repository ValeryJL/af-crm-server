package com.afcrm.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ot", unique = true, nullable = false)
    private String numeroOt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkOrderType tipo = WorkOrderType.PREVENTIVO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkOrderStatus estado = WorkOrderStatus.PENDIENTE;

    @Column(name = "fecha_programada")
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(columnDefinition = "text")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnore
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private User tecnicoAsignado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data_reporte", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> formDataReporte = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "partes_reemplazadas", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> partesReemplazadas = new HashMap<>();

    @Column(name = "firma_cliente_nombre")
    private String firmaClienteNombre;

    @Column(name = "firma_cliente_dni")
    private String firmaClienteDni;

    @Column(name = "observaciones_cierre", columnDefinition = "text")
    private String observacionesCierre;
}
