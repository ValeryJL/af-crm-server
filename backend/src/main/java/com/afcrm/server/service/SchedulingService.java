package com.afcrm.server.service;

import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.ServiceFrequency;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import com.afcrm.server.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SchedulingService {

    private final ScheduledTaskRepository scheduledTaskRepository;

    public void generateTasksForService(Service service, int generationMonths) {
        LocalDate startDate = service.getAlta() != null ? service.getAlta() : LocalDate.now();
        generateTasksForService(service, generationMonths, startDate);
    }

    public void generateTasksForService(Service service, int generationMonths, LocalDate startDate) {
        if (service.getBaja() != null && service.getBaja().isBefore(LocalDate.now())) {
            return; // Inactive service
        }

        LocalDate endDate = LocalDate.now().plusMonths(generationMonths);
        if (service.getBaja() != null && service.getBaja().isBefore(endDate)) {
            endDate = service.getBaja();
        }

        List<ScheduledTask> tasks = new ArrayList<>();
        LocalDate currentDate = startDate;

        ServiceFrequency freq = service.getFrecuencia() != null ? service.getFrecuencia() : ServiceFrequency.EVENTUAL;

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            ScheduledTask task = ScheduledTask.builder()
                    .service(service)
                    .scheduledDate(currentDate)
                    .status(TaskStatus.PENDING)
                    .type(freq == ServiceFrequency.EVENTUAL ? TaskType.EVENTUAL : TaskType.REGULAR)
                    .build();
            tasks.add(task);

            switch (freq) {
                case WEEKLY:
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case FORTNIGHTLY:
                    currentDate = currentDate.plusWeeks(2);
                    break;
                case MONTHLY:
                    currentDate = currentDate.plusMonths(1);
                    break;
                default:
                    // Eventual or unknown
                    currentDate = endDate.plusDays(1);
                    break;
            }
        }

        scheduledTaskRepository.saveAll(tasks);
    }

    @Transactional
    public void rescheduleService(Service service, int generationMonths) {
        LocalDate today = LocalDate.now();
        // Delete future pending tasks
        scheduledTaskRepository.deleteByServiceAndScheduledDateAfterAndStatus(service, today, TaskStatus.PENDING);
        // Generate new ones starting from tomorrow to avoid duplicating today's task if it exists
        LocalDate startDate = today.plusDays(1);
        generateTasksForService(service, generationMonths, startDate);
    }

    public void deactivateService(Service service, LocalDate bajaDate) {
        service.setBaja(bajaDate);
        List<ScheduledTask> pendingTasks = scheduledTaskRepository.findByServiceAndScheduledDateAfterAndStatus(service, bajaDate, TaskStatus.PENDING);
        for (ScheduledTask task : pendingTasks) {
            task.setStatus(TaskStatus.CANCELLED);
        }
        scheduledTaskRepository.saveAll(pendingTasks);
    }

    public void reprogramTask(Long taskId, LocalDate newDate) {
        scheduledTaskRepository.findById(taskId).ifPresent(task -> {
            task.setScheduledDate(newDate);
            scheduledTaskRepository.save(task);
        });
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> ids, TaskStatus status) {
        List<ScheduledTask> tasks = scheduledTaskRepository.findAllById(ids);
        for (ScheduledTask task : tasks) {
            task.setStatus(status);
        }
        scheduledTaskRepository.saveAll(tasks);
    }
}
