package com.afcrm.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipment_parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentPart extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreParte;

    @Column(name = "codigo_repuesto")
    private String codigoRepuesto;

    @Column(name = "fecha_ultimo_cambio")
    private LocalDate fechaUltimoCambio;

    @Column(name = "vida_util_meses")
    private Integer vidaUtilMeses;

    @Column(name = "vida_util_horas")
    private Double vidaUtilHoras;

    @Column(name = "alerta_temprana_dias")
    @Builder.Default
    private Integer alertaTempranaDias = 15;

    @Column(columnDefinition = "text")
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @JsonIgnore
    private Equipment equipment;
}

