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
    private LocalDate fechaProximoServiceMayor;
    private Integer intervaloHorasService;
    private Map<String, Object> specs;
    private String notas;
    private boolean activo;
    private Long serviceId;
    private String serviceNombre;
    private String clientRazonSocial;
    private List<EquipmentPartDto> parts;
}

