package com.afcrm.server.controller;

import com.afcrm.server.dto.UserConfigDto;
import com.afcrm.server.dto.UserDto;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.UserRepository;
import com.afcrm.server.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole())
                .nombre(userDto.getNombre())
                .apellido(userDto.getApellido())
                .telefono(userDto.getTelefono())
                .status(userDto.getStatus())
                .customConfiguration(userDto.getCustomConfiguration())
                .build();

        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/me/config")
    public ResponseEntity<UserConfigDto> getMeConfig(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserConfigDto dto = UserConfigDto.builder()
                .oauthEnabled(user.isOauthEnabled())
                .theme(user.getTheme())
                .customConfiguration(user.getCustomConfiguration())
                .build();
        
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me/config")
    public ResponseEntity<?> updateMeConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserConfigDto configDto) {
        
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (configDto.getTheme() != null) {
            user.setTheme(configDto.getTheme());
        }
        
        // As requested: enable/disable oauth login
        if (configDto.getOauthEnabled() != null) {
            user.setOauthEnabled(configDto.getOauthEnabled());
        }
        
        if (configDto.getCustomConfiguration() != null) {
            user.setCustomConfiguration(configDto.getCustomConfiguration());
        }
        
        return ResponseEntity.ok(userRepository.save(user));
    }
}
