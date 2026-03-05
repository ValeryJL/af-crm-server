package com.afcrm.server.controller;

import com.afcrm.server.dto.DashboardStatsDto;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.repository.EventualTaskRepository;
import com.afcrm.server.repository.ScheduledTaskRepository;
import com.afcrm.server.repository.ServiceRepository;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final EventualTaskRepository eventualTaskRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        long activeServices = serviceRepository.countByBajaIsNull();
        long availableTechnicians = userRepository.countByStatus("ACTIVE");
        
        long pendingScheduled = scheduledTaskRepository.countByStatus(TaskStatus.PENDING);
        long pendingEventual = eventualTaskRepository.countByStatus(TaskStatus.PENDING);
        
        DashboardStatsDto stats = DashboardStatsDto.builder()
                .activeServicesCount(activeServices)
                .availableTechniciansCount(availableTechnicians)
                .pendingTasksCount(pendingScheduled + pendingEventual)
                .build();
                
        return ResponseEntity.ok(stats);
    }
}
