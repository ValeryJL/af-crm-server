package com.afcrm.server.controller;

import com.afcrm.server.dto.UserConfigDto;
import com.afcrm.server.dto.UserDto;
import com.afcrm.server.dto.UserUpdateDto;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.user.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.user.id")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, 
            @RequestBody UserUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userRepository.findById(id).map(user -> {
            if (dto.getNombre() != null) user.setNombre(dto.getNombre());
            if (dto.getApellido() != null) user.setApellido(dto.getApellido());
            if (dto.getEmail() != null) user.setEmail(dto.getEmail());
            if (dto.getTelefono() != null) user.setTelefono(dto.getTelefono());
            if (dto.getStatus() != null) user.setStatus(dto.getStatus());
            if (dto.getTheme() != null) user.setTheme(dto.getTheme());
            if (dto.getCustomConfiguration() != null) user.setCustomConfiguration(dto.getCustomConfiguration());
            
            // Only admin can change roles
            if (dto.getRole() != null && "ADMIN".equals(userDetails.getUser().getRole().name())) {
                user.setRole(dto.getRole());
            }

            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.user.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
