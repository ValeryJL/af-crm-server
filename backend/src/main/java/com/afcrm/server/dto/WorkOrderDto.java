package com.afcrm.server.dto;

import com.afcrm.server.model.WorkOrderStatus;
import com.afcrm.server.model.WorkOrderType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderDto {
    private Long id;
    private String numeroOt;
    private WorkOrderType tipo;
    private WorkOrderStatus estado;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaInicioReal;
    private LocalDateTime fechaFinalizacion;
    private String descripcion;
    private Long serviceId;
    private String serviceNombre;
    private String clientRazonSocial;
    private String direccionFisica;
    private Long tecnicoId;
    private String tecnicoNombre;
    private Map<String, Object> formDataReporte;
    private Map<String, Object> partesReemplazadas;
    private String firmaClienteNombre;
    private String firmaClienteDni;
    private String observacionesCierre;
}

