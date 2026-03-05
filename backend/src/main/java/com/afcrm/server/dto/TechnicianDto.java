package com.afcrm.server.dto;

import com.afcrm.server.model.Role;
import lombok.Data;

@Data
public class TechnicianDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Role role;
    private String telefono;
    private String status;
}
