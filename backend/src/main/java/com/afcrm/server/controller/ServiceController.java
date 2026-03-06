package com.afcrm.server.controller;

import com.afcrm.server.dto.ServiceDto;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.ServiceFrequency;
import com.afcrm.server.repository.GroupRepository;
import com.afcrm.server.repository.ServiceRepository;
import com.afcrm.server.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final GroupRepository groupRepository;
    private final SchedulingService schedulingService;
    private final com.afcrm.server.repository.ScheduledTaskRepository scheduledTaskRepository;

    @GetMapping
    public List<ServiceDto> getAll() {
        return serviceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getById(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ServiceDto create(@RequestBody ServiceDto dto) {
        Service service = new Service();
        mapToEntity(dto, service);
        Service saved = serviceRepository.save(service);
        schedulingService.generateTasksForService(saved, 12); // Auto-generate 12 months
        return mapToDto(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ServiceDto> update(@PathVariable Long id, @RequestBody ServiceDto dto) {
        return serviceRepository.findById(id)
                .map(service -> {
                    ServiceFrequency oldFreq = service.getFrecuencia();
                    boolean wasInactive = service.getBaja() != null;
                    mapToEntity(dto, service);
                    Service updated = serviceRepository.save(service);
                    if (updated.getBaja() != null) {
                        // Deactivating: cancel future pending tasks
                        schedulingService.deactivateService(updated, updated.getBaja());
                    } else if (wasInactive || (oldFreq != updated.getFrecuencia())) {
                        // Reactivating or frequency changed: regenerate schedule
                        schedulingService.rescheduleService(updated, 12);
                    }
                    return ResponseEntity.ok(mapToDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return serviceRepository.findById(id).map(service -> {
            // Delete all child tasks first to avoid FK constraint
            scheduledTaskRepository.deleteByService(service);
            serviceRepository.delete(service);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private void mapToEntity(ServiceDto dto, Service service) {
        service.setNombre(dto.getNombre());
        service.setDireccion(dto.getDireccion());
        service.setTipo(dto.getTipo());
        
        service.setFrecuencia(ServiceFrequency.fromString(dto.getFrecuencia()));

        service.setObservaciones(dto.getObservaciones());
        service.setPlanilla(dto.getPlanilla());
        service.setCliente(dto.getCliente());
        service.setContactos(dto.getContactos());
        service.setRequerimientos(dto.getRequerimientos());
        service.setAlta(dto.getAlta() != null ? dto.getAlta() : LocalDate.now());
        service.setBaja(dto.getBaja());

        if (dto.getGroupId() != null) {
             groupRepository.findById(dto.getGroupId()).ifPresent(service::setGroup);
        }
    }

    private ServiceDto mapToDto(Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setNombre(service.getNombre());
        dto.setDireccion(service.getDireccion());
        dto.setTipo(service.getTipo());
        dto.setFrecuencia(service.getFrecuencia() != null ? service.getFrecuencia().getValue() : null);
        dto.setObservaciones(service.getObservaciones());
        dto.setPlanilla(service.getPlanilla());
        dto.setCliente(service.getCliente());
        dto.setContactos(service.getContactos());
        dto.setRequerimientos(service.getRequerimientos());
        dto.setAlta(service.getAlta());
        dto.setBaja(service.getBaja());
        if (service.getGroup() != null) {
            dto.setGroupId(service.getGroup().getId());
        }
        return dto;
    }
}
