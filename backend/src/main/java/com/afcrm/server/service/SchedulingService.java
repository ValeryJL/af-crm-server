package com.afcrm.server.service;

import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import com.afcrm.server.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SchedulingService {

    private final ScheduledTaskRepository scheduledTaskRepository;

    public void generateTasksForService(Service service, int generationMonths) {
        if (service.getBaja() != null && service.getBaja().isBefore(LocalDate.now())) {
            return; // Inactive service
        }

        LocalDate startDate = service.getAlta() != null ? service.getAlta() : LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(generationMonths);

        if (service.getBaja() != null && service.getBaja().isBefore(endDate)) {
            endDate = service.getBaja();
        }

        List<ScheduledTask> tasks = new ArrayList<>();
        LocalDate currentDate = startDate;

        String freq = service.getFrecuencia() != null ? service.getFrecuencia().toLowerCase() : "eventual";

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            ScheduledTask task = ScheduledTask.builder()
                    .service(service)
                    .scheduledDate(currentDate)
                    .status(TaskStatus.PENDING)
                    .type(freq.equals("eventual") ? TaskType.EVENTUAL : TaskType.REGULAR)
                    .build();
            tasks.add(task);

            switch (freq) {
                case "semanal":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case "quincenal":
                    currentDate = currentDate.plusWeeks(2);
                    break;
                case "mensual":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "bimestral":
                    currentDate = currentDate.plusMonths(2);
                    break;
                case "trimestral":
                    currentDate = currentDate.plusMonths(3);
                    break;
                case "semestral":
                    currentDate = currentDate.plusMonths(6);
                    break;
                case "anual":
                    currentDate = currentDate.plusYears(1);
                    break;
                default:
                    // Eventual or unknown, break loop to avoid infinite execution
                    currentDate = endDate.plusDays(1);
                    break;
            }
        }

        scheduledTaskRepository.saveAll(tasks);
    }

    public void deactivateService(Service service, LocalDate bajaDate) {
        service.setBaja(bajaDate);
        List<ScheduledTask> pendingTasks = scheduledTaskRepository.findByServiceAndScheduledDateAfter(service, bajaDate);
        for (ScheduledTask task : pendingTasks) {
            if (task.getStatus() == TaskStatus.PENDING) {
                task.setStatus(TaskStatus.CANCELLED);
            }
        }
        scheduledTaskRepository.saveAll(pendingTasks);
    }
}
