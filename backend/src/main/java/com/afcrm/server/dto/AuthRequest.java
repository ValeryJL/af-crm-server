package com.afcrm.server.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String idToken; // For Google OAuth2 validation
}
