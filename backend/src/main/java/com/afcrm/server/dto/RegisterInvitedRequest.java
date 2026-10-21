package com.afcrm.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterInvitedRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String nombre;
    private String apellido;
    private String telefono;
}
