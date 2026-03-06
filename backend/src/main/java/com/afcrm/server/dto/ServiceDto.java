package com.afcrm.server.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceDto {
    private Long id;
    private String nombre;
    private String direccion;
    private String tipo;
    private String frecuencia;
    private String observaciones;
    private String planilla;
    private String cliente;
    private String contactos;
    private String requerimientos;
    private LocalDate alta;
    private LocalDate baja;
    private Long groupId;
}
