package com.afcrm.server.controller;

import com.afcrm.server.dto.TechnicianDto;
import com.afcrm.server.model.Technician;
import com.afcrm.server.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianRepository technicianRepository;

    @GetMapping
    public List<TechnicianDto> getAll() {
        return technicianRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianDto> getById(@PathVariable Long id) {
        return technicianRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TechnicianDto create(@RequestBody TechnicianDto dto) {
        Technician technician = new Technician();
        technician.setNombre(dto.getNombre());
        technician.setApellido(dto.getApellido());
        technician.setMail(dto.getMail());
        technician.setTelefono(dto.getTelefono());
        return mapToDto(technicianRepository.save(technician));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicianDto> update(@PathVariable Long id, @RequestBody TechnicianDto dto) {
        return technicianRepository.findById(id)
                .map(technician -> {
                    technician.setNombre(dto.getNombre());
                    technician.setApellido(dto.getApellido());
                    technician.setMail(dto.getMail());
                    technician.setTelefono(dto.getTelefono());
                    return ResponseEntity.ok(mapToDto(technicianRepository.save(technician)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!technicianRepository.existsById(id)) return ResponseEntity.notFound().build();
        technicianRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TechnicianDto mapToDto(Technician technician) {
        TechnicianDto dto = new TechnicianDto();
        dto.setId(technician.getId());
        dto.setNombre(technician.getNombre());
        dto.setApellido(technician.getApellido());
        dto.setMail(technician.getMail());
        dto.setTelefono(technician.getTelefono());
        return dto;
    }
}
