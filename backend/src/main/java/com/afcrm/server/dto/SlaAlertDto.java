package com.afcrm.server.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaAlertDto {
    private String tipo; // "CRITICAL_OVERDUE", "UPCOMING_SLA", "CONSUMABLE_EXPIRED"
    private String severidad; // "RED", "YELLOW", "ORANGE"
    private String titulo;
    private String descripcion;
    private Long clientId;
    private String clientRazonSocial;
    private Long serviceId;
    private String serviceNombre;
    private Long equipmentId;
    private String equipmentNombre;
    private LocalDate fechaLimite;
    private Long diasDiferencia;
    private String accionSugerida;
}

