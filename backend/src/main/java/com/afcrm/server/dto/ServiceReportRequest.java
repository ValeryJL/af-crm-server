package com.afcrm.server.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@Data
public class ServiceReportRequest {
    private Long scheduledTaskId;
    private LocalDate fecha;
    private LocalTime hora;
    private String tipo;
    private String observaciones;
    private String tipoPrueba;
    private Boolean completar;
    
    // Techs / Groups logic depending on requirement, sending IDs
    private Long technicianId;
    private Set<Long> taskPartIds;
    
    // Massive JSON payload mapping
    private Map<String, Object> measurements;
}
