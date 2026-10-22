package com.afcrm.server.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentPartDto {
    private Long id;
    private String nombreParte;
    private String codigoRepuesto;
    private LocalDate fechaUltimoCambio;
    private Integer vidaUtilMeses;
    private Double vidaUtilHoras;
    private Integer alertaTempranaDias;
    private String notas;
    private Long equipmentId;
    private String equipmentNombre;
    private LocalDate fechaVencimientoEstimada;
    private Boolean vencido;
}

