package com.afcrm.server.dto;

import lombok.Data;

@Data
public class TechnicianDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String mail;
    private String telefono;
}
