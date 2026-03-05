package com.afcrm.server.controller;

import com.afcrm.server.dto.TechnicianDto;
import com.afcrm.server.model.Role;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<TechnicianDto> getAll() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianDto> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TechnicianDto create(@RequestBody TechnicianDto dto) {
        User user = new User();
        user.setNombre(dto.getNombre());
        user.setApellido(dto.getApellido());
        user.setEmail(dto.getEmail());
        user.setTelefono(dto.getTelefono());
        user.setStatus(dto.getStatus());
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.TECH);
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("default123")); // Fallback if no password provided
        }
        return mapToDto(userRepository.save(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicianDto> update(@PathVariable Long id, @RequestBody TechnicianDto dto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNombre(dto.getNombre());
                    user.setApellido(dto.getApellido());
                    user.setEmail(dto.getEmail());
                    user.setTelefono(dto.getTelefono());
                    user.setStatus(dto.getStatus());
                    if (dto.getRole() != null) {
                        user.setRole(dto.getRole());
                    }
                    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    return ResponseEntity.ok(mapToDto(userRepository.save(user)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TechnicianDto mapToDto(User user) {
        TechnicianDto dto = new TechnicianDto();
        dto.setId(user.getId());
        dto.setNombre(user.getNombre());
        dto.setApellido(user.getApellido());
        dto.setEmail(user.getEmail());
        dto.setTelefono(user.getTelefono());
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole());
        return dto;
    }
}
