package com.afcrm.server.dto;

import com.afcrm.server.model.ServiceFrequency;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDto {
    private Long id;
    private String nombre;
    private String direccionFisica;
    private String localidad;
    private String provincia;
    private ServiceFrequency frecuenciaVisita;
    private Integer diasAvisoAlerta;
    private LocalDate fechaUltimaVisita;
    private LocalDate fechaProximaVisitaLimite;
    private String notas;
    private Boolean activo;
    private Long clientId;
    private String clientRazonSocial;
    private Integer totalEquipos;
    private String slaStatus; // "OVERDUE", "UPCOMING", "ON_TIME"
    private Long diasParaVisita;
    private List<EquipmentDto> equipments;
    private List<ContactDto> contacts;
}

