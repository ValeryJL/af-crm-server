package com.afcrm.server.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDto {
    private Long id;
    private String nombre;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String ubicacion;
    private Double horometroActual;
    
    // Periodicidades en equipos
    private String periodicidadRegular; // e.g. "MENSUAL", "BIMESTRAL", "QUINCENAL", "TRIMESTRAL", "SEMANAL"
    private Integer intervaloHorasRegular; // e.g. 250 hs
    private LocalDate fechaUltimaVisitaRegular;
    private LocalDate fechaProximaVisitaRegular;
    
    private String periodicidadServiceMayor; // e.g. "ANUAL", "SEMESTRAL"
    private LocalDate fechaUltimoServiceMayor;
    private LocalDate fechaProximoServiceMayor;
    private Integer intervaloHorasService; // e.g. 1000 hs
    
    private Map<String, Object> specs;
    private String notas;
    private Boolean activo;
    private Long serviceId;
    private String serviceNombre;
    private String clientRazonSocial;
    private List<EquipmentPartDto> parts;
}
