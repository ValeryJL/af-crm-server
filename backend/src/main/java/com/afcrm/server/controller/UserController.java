package com.afcrm.server.controller;

import com.afcrm.server.dto.InvitationRequest;
import com.afcrm.server.dto.UserConfigDto;
import com.afcrm.server.dto.UserDto;
import com.afcrm.server.dto.UserUpdateDto;
import com.afcrm.server.model.Invitation;
import com.afcrm.server.model.Role;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.InvitationRepository;
import com.afcrm.server.repository.UserRepository;
import com.afcrm.server.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == principal.user.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == principal.user.id")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, 
            @RequestBody UserUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        return userRepository.findById(id).map(targetUser -> {
            User currentUser = userDetails.getUser();
            Role currentRole = currentUser.getRole();
            Role targetRole = targetUser.getRole();

            // 1. SUPER_ADMIN Protection: No one can change the role of the SUPER_ADMIN
            if (targetRole == Role.SUPER_ADMIN && dto.getRole() != null && dto.getRole() != Role.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change role of SUPER_ADMIN");
            }

            // 2. Only SUPER_ADMIN can change their own password/details OR if user matches
            if (targetRole == Role.SUPER_ADMIN && currentUser.getId() != targetUser.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can edit themselves");
            }

            // 3. ADMIN restrictions
            if (currentRole == Role.ADMIN) {
                // Cannot edit SUPER_ADMIN (already covered by above)
                if (targetRole == Role.SUPER_ADMIN) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN cannot edit SUPER_ADMIN");
                }
                // Cannot edit other ADMINs
                if (targetRole == Role.ADMIN && currentUser.getId() != targetUser.getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN cannot edit other ADMINs");
                }
                // Cannot downgrade themselves
                if (currentUser.getId() == targetUser.getId() && dto.getRole() != null && dto.getRole() != Role.ADMIN) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN cannot downgrade themselves");
                }
            }

            // Apply updates
            if (dto.getNombre() != null) targetUser.setNombre(dto.getNombre());
            if (dto.getApellido() != null) targetUser.setApellido(dto.getApellido());
            if (dto.getEmail() != null) targetUser.setEmail(dto.getEmail());
            if (dto.getTelefono() != null) targetUser.setTelefono(dto.getTelefono());
            if (dto.getStatus() != null) targetUser.setStatus(dto.getStatus());
            if (dto.getTheme() != null) targetUser.setTheme(dto.getTheme());
            if (dto.getCustomConfiguration() != null) targetUser.setCustomConfiguration(dto.getCustomConfiguration());

            // Role promotion/demote logic
            if (dto.getRole() != null) {
                if (currentRole == Role.SUPER_ADMIN) {
                    targetUser.setRole(dto.getRole());
                } else if (currentRole == Role.ADMIN) {
                    // ADMIN can promote TECH to ADMIN, or keep ADMIN
                    if (targetRole == Role.TECH && dto.getRole() == Role.ADMIN) {
                        targetUser.setRole(Role.ADMIN);
                    }
                }
            }

            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                targetUser.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            return ResponseEntity.ok(userRepository.save(targetUser));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == principal.user.id")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        return userRepository.findById(id).map(targetUser -> {
            User currentUser = userDetails.getUser();
            Role currentRole = currentUser.getRole();
            Role targetRole = targetUser.getRole();

            // 1. SUPER_ADMIN cannot be deleted
            if (targetRole == Role.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("SUPER_ADMIN cannot be deleted");
            }

            // 2. ADMIN restrictions
            if (currentRole == Role.ADMIN) {
                // Cannot delete other ADMINs
                if (targetRole == Role.ADMIN && currentUser.getId() != targetUser.getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN cannot delete other ADMINs");
                }
                // Cannot delete themselves? (Requirement: "ADMIN ... CANNOT downgrade themselves")
                // Deletion is the ultimate downgrade.
                if (currentUser.getId() == targetUser.getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN cannot delete themselves");
                }
            }

            userRepository.delete(targetUser);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> inviteUser(@RequestBody InvitationRequest request) {
        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.builder()
                .token(token)
                .role(request.getRole())
                .expiresAt(LocalDateTime.now().plusHours(2))
                .used(false)
                .build();
        invitationRepository.save(invitation);
        return ResponseEntity.ok(token);
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
