package com.afcrm.server.dto;

import com.afcrm.server.model.Role;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String email;
    private String password;
    private Role role;
    private String nombre;
    private String apellido;
    private String telefono;
    private String status;
    private String theme;
    private String customConfiguration;
}
