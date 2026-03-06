package com.afcrm.server.controller;

import com.afcrm.server.dto.CalendarEventDto;
import com.afcrm.server.dto.CalendarFormDataDto;
import com.afcrm.server.dto.CalendarTaskDto;
import com.afcrm.server.dto.BulkStatusUpdateRequest;
import com.afcrm.server.dto.EventualTaskRequest;
import com.afcrm.server.model.EventualTask;
import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import com.afcrm.server.repository.EventualTaskRepository;
import com.afcrm.server.repository.ScheduledTaskRepository;
import com.afcrm.server.repository.ServiceRepository;
import com.afcrm.server.service.CalendarEventService;
import com.afcrm.server.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final EventualTaskRepository eventualTaskRepository;
    private final ServiceRepository serviceRepository;
    private final CalendarEventService calendarEventService;
    private final SchedulingService schedulingService;

    @GetMapping
    public List<CalendarTaskDto> getTasks(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        // In a real scenario, we would filter by the logged-in user if they are a TECH.
        // For now, grabbing the range.
        return scheduledTaskRepository.findByScheduledDateBetween(start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/eventual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CalendarTaskDto> createEventual(@RequestBody EventualTaskRequest request) {
        return serviceRepository.findById(request.getServiceId())
                .map(service -> {
                    // Create legacy eventual record
                    EventualTask eventual = EventualTask.builder()
                            .descripcion(request.getDescripcion())
                            .fecha(request.getFecha())
                            .service(service)
                            .build();
                    eventualTaskRepository.save(eventual);

                    // Create calendar node
                    ScheduledTask task = ScheduledTask.builder()
                            .scheduledDate(request.getFecha())
                            .type(TaskType.EVENTUAL)
                            .status(TaskStatus.PENDING)
                            .service(service)
                            .build();
                    ScheduledTask saved = scheduledTaskRepository.save(task);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<CalendarTaskDto> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> updates) {
        return scheduledTaskRepository.findById(id)
                .map(task -> {
                    if (updates.containsKey("status")) {
                        task.setStatus(TaskStatus.valueOf(updates.get("status")));
                    }
                    if (updates.containsKey("scheduledDate")) {
                        task.setScheduledDate(LocalDate.parse(updates.get("scheduledDate")));
                    }
                    return ResponseEntity.ok(mapToDto(scheduledTaskRepository.save(task)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/tasks/{id}/reprogram")
    public ResponseEntity<Void> reprogramTask(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        if (!payload.containsKey("newDate")) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate newDate = LocalDateTime.parse(payload.get("newDate").replace("Z", "")).toLocalDate();
        schedulingService.reprogramTask(id, newDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/form-data")
    public ResponseEntity<CalendarFormDataDto> getFormData(@PathVariable Long id) {
        return scheduledTaskRepository.findById(id)
                .map(task -> {
                    var service = task.getService();
                    var group = service != null ? service.getGroup() : null;
                    return ResponseEntity.ok(CalendarFormDataDto.builder()
                            .id(task.getId())
                            .serviceName(service != null ? service.getNombre() : null)
                            .address(service != null ? service.getDireccion() : null)
                            .client(service != null ? service.getCliente() : null)
                            .groupBrand(group != null ? group.getMarca() : null)
                            .groupModel(group != null ? group.getModelo() : null)
                            .build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/tasks/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatus(@RequestBody BulkStatusUpdateRequest request) {
        schedulingService.bulkUpdateStatus(request.getIds(), request.getStatus());
        return ResponseEntity.ok().build();
    }

    private CalendarTaskDto mapToDto(ScheduledTask task) {
        return CalendarTaskDto.builder()
                .id(task.getId())
                .scheduledDate(task.getScheduledDate())
                .status(task.getStatus())
                .type(task.getType())
                .serviceId(task.getService() != null ? task.getService().getId() : null)
                .serviceName(task.getService() != null ? task.getService().getNombre() : null)
                .build();
    }
}
