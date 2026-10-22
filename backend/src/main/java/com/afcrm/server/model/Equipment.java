package com.afcrm.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String marca;

    private String modelo;

    @Column(name = "numero_serie")
    private String numeroSerie;

    private String ubicacion;

    @Column(name = "horometro_actual")
    private Double horometroActual;

    @Column(name = "periodicidad_regular")
    private String periodicidadRegular;

    @Column(name = "intervalo_horas_regular")
    private Integer intervaloHorasRegular;

    @Column(name = "fecha_ultima_visita_regular")
    private LocalDate fechaUltimaVisitaRegular;

    @Column(name = "fecha_proxima_visita_regular")
    private LocalDate fechaProximaVisitaRegular;

    @Column(name = "periodicidad_service_mayor")
    private String periodicidadServiceMayor;

    @Column(name = "fecha_ultimo_service_mayor")
    private LocalDate fechaUltimoServiceMayor;

    @Column(name = "fecha_proximo_service_mayor")
    private LocalDate fechaProximoServiceMayor;

    @Column(name = "intervalo_horas_service")
    private Integer intervaloHorasService;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> specs = new HashMap<>();

    @Column(columnDefinition = "text")
    private String notas;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnore
    private Service service;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EquipmentPart> parts = new ArrayList<>();
}
