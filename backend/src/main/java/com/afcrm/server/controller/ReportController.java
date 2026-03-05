package com.afcrm.server.controller;

import com.afcrm.server.dto.ServiceReportRequest;
import com.afcrm.server.model.ServiceReport;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.repository.ScheduledTaskRepository;
import com.afcrm.server.repository.ServiceReportRepository;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ServiceReportRepository reportRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ServiceReportRequest request) {
        return scheduledTaskRepository.findById(request.getScheduledTaskId())
                .map(task -> {
                    if (task.getStatus() == TaskStatus.CANCELLED) {
                        return ResponseEntity.badRequest().body("Cannot submit report for a CANCELLED task.");
                    }

                    // Map entity
                    ServiceReport report = ServiceReport.builder()
                            .scheduledTask(task)
                            .fecha(request.getFecha())
                            .hora(request.getHora())
                            .tipo(request.getTipo())
                            .tipoPrueba(request.getTipoPrueba())
                            .observaciones(request.getObservaciones())
                            .completar(request.getCompletar())
                            .measurements(request.getMeasurements())
                            .build();

                    // Optional links
                    if (request.getTechnicianId() != null) {
                        userRepository.findById(request.getTechnicianId()).ifPresent(report::setUser);
                    }

                    reportRepository.save(report);

                    // Crucial Step: Auto-Complete Calendar Node
                    task.setStatus(TaskStatus.COMPLETED);
                    scheduledTaskRepository.save(task);

                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
