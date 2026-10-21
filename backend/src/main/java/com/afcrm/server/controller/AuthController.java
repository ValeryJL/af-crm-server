package com.afcrm.server.controller;

import com.afcrm.server.dto.AuthRequest;
import com.afcrm.server.dto.GoogleLoginRequest;
import com.afcrm.server.dto.AuthResponse;
import com.afcrm.server.dto.RegisterInvitedRequest;
import com.afcrm.server.dto.UserDto;
import com.afcrm.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody UserDto request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @PostMapping("/register-invited")
    public ResponseEntity<AuthResponse> registerInvited(@Valid @RequestBody RegisterInvitedRequest request) {
        return ResponseEntity.ok(authService.registerInvited(request));
    }

    @GetMapping("/setup-status")
    public ResponseEntity<?> getSetupStatus() {
        return ResponseEntity.ok(Collections.singletonMap("setupRequired", authService.isSetupRequired()));
    }
}
