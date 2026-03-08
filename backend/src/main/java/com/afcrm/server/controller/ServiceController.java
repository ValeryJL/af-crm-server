package com.afcrm.server.controller;

import com.afcrm.server.dto.CalendarTaskDto;
import com.afcrm.server.dto.ServiceDto;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.ServiceFrequency;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import com.afcrm.server.repository.GroupRepository;
import com.afcrm.server.repository.ServiceRepository;
import com.afcrm.server.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<CalendarTaskDto>> getTasksForService(
            @PathVariable Long id,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return serviceRepository.findById(id)
                .map(service -> {
                    java.time.LocalDateTime fromTime = from != null ? from.atStartOfDay() : null;
                    java.time.LocalDateTime toTime = to != null ? to.atTime(23, 59, 59) : null;
                    List<CalendarTaskDto> tasks = scheduledTaskRepository
                            .findTasksByService(
                                    service,
                                    status != null, status,
                                    type != null, type,
                                    fromTime != null, fromTime,
                                    toTime != null, toTime
                            )
                            .stream()
                            .map(t -> CalendarTaskDto.builder()
                                    .id(t.getId())
                                    .fechaProgramada(t.getFechaProgramada())
                                    .reportID(t.getReportID())
                                    .status(t.getStatus())
                                    .type(t.getType())
                                    .serviceId(service.getId())
                                    .serviceName(service.getNombre())
                                    .build())
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(tasks);
                })
                .orElse(ResponseEntity.notFound().build());
    }

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
                    boolean wasInactive = service.getFechaFin() != null;
                    mapToEntity(dto, service);
                    Service updated = serviceRepository.save(service);
                    if (updated.getFechaFin() != null) {
                        // Deactivating: cancel future pending tasks
                        schedulingService.deactivateService(updated, updated.getFechaFin());
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
        service.setEquipo(dto.getEquipo());
        service.setServiceToggle(dto.isServiceToggle());
        service.setFechaPrimerService(dto.getFechaPrimerService());
        service.setRequerimientos(dto.getRequerimientos());
        service.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now());
        service.setFechaFin(dto.getFechaFin());

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
        dto.setEquipo(service.getEquipo());
        dto.setServiceToggle(service.isServiceToggle());
        dto.setFechaPrimerService(service.getFechaPrimerService());
        dto.setRequerimientos(service.getRequerimientos());
        dto.setFechaInicio(service.getFechaInicio());
        dto.setFechaFin(service.getFechaFin());
        if (service.getGroup() != null) {
            dto.setGroupId(service.getGroup().getId());
        }
        return dto;
    }
}
