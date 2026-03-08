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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SchedulingService {

    private final ScheduledTaskRepository scheduledTaskRepository;

    public void generateTasksForService(Service service, int generationMonths) {
        LocalDate startDate = service.getFechaInicio() != null ? service.getFechaInicio() : LocalDate.now();
        generateTasksForService(service, generationMonths, startDate);
    }

    public void generateTasksForService(Service service, int generationMonths, LocalDate startDate) {
        if (service.getFechaFin() != null && service.getFechaFin().isBefore(LocalDate.now())) {
            return; // Inactive service
        }

        LocalDate endDate = service.getFechaFin() != null ? service.getFechaFin()
                : LocalDate.now().plusMonths(generationMonths);

        List<ScheduledTask> tasks = new ArrayList<>();
        LocalDate currentDate = startDate;

        ServiceFrequency freq = service.getFrecuencia() != null ? service.getFrecuencia() : ServiceFrequency.EVENTUAL;

        // --- Maintenance / Eventual tasks (fechaProgramada = null, UNASSIGNED) ---
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            LocalDate normalizedPeriod = calculatePeriodDate(currentDate.atStartOfDay(), freq);

            System.out.println("DEBUG: Generating task for service " + service.getId() + " freq=" + freq + " date=" + currentDate + " normalized=" + normalizedPeriod);

            ScheduledTask task = ScheduledTask.builder()
                    .service(service)
                    .fechaProgramada(null)        // UNASSIGNED: no date yet
                    .periodDate(normalizedPeriod)  // tracks which period this belongs to
                    .status(TaskStatus.UNASSIGNED)
                    .type(freq == ServiceFrequency.EVENTUAL ? TaskType.EVENTUAL : TaskType.MAINTENANCE)
                    .build();
            tasks.add(task);

            switch (freq) {
                case WEEKLY:
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case FIFTEEN_DAYS:
                    currentDate = currentDate.plusWeeks(2);
                    break;
                case MONTHLY:
                    currentDate = currentDate.plusMonths(1);
                    break;
                default:
                    currentDate = endDate.plusDays(1); // EVENTUAL: single task, break loop
                    break;
            }
        }

        // --- Yearly SERVICE events (fechaProgramada pre-assigned, status PENDING) ---
        if (service.isServiceToggle()) {
            // First event: use fechaPrimerService if provided, else fechaInicio + 1 year
            LocalDate firstServiceDate = service.getFechaPrimerService() != null
                    ? service.getFechaPrimerService()
                    : startDate.plusYears(1);

            LocalDate anniversary = firstServiceDate;
            while (anniversary.isBefore(endDate) || anniversary.isEqual(endDate)) {
                ScheduledTask serviceTask = ScheduledTask.builder()
                        .service(service)
                        .fechaProgramada(anniversary.atStartOfDay()) // pre-assigned date
                        .periodDate(anniversary)
                        .status(TaskStatus.PENDING)  // already has a date → PENDING
                        .type(TaskType.SERVICE)
                        .build();
                tasks.add(serviceTask);
                anniversary = anniversary.plusYears(1);
            }
        }

        scheduledTaskRepository.saveAll(tasks);
    }

    @Transactional
    public void rescheduleService(Service service, int generationMonths) {
        LocalDate today = LocalDate.now();
        // Delete all future unresolved tasks (both PENDING by fechaProgramada and all UNASSIGNED)
        scheduledTaskRepository.deleteByServiceAndFechaProgramadaAfterAndStatus(
                service, today.atStartOfDay(), TaskStatus.PENDING);
        scheduledTaskRepository.deleteByServiceAndStatus(service, TaskStatus.UNASSIGNED);

        LocalDate startDate = today.plusDays(1);
        generateTasksForService(service, generationMonths, startDate);
    }

    public void deactivateService(Service service, LocalDate fechaFinDate) {
        service.setFechaFin(fechaFinDate);
        List<ScheduledTask> pendingTasks = scheduledTaskRepository
                .findByServiceAndFechaProgramadaAfterAndStatus(
                        service, fechaFinDate.atStartOfDay(), TaskStatus.PENDING);
        for (ScheduledTask task : pendingTasks) {
            task.setStatus(TaskStatus.CANCELLED);
        }
        scheduledTaskRepository.saveAll(pendingTasks);
    }

    public void reprogramTask(Long taskId, LocalDateTime newDateTime) {
        scheduledTaskRepository.findById(taskId).ifPresent(task -> {
            task.setFechaProgramada(newDateTime);
            // Updating periodDate to match the new programmed week/month
            if (task.getService() != null) {
                ServiceFrequency freq = task.getService().getFrecuencia() != null ? task.getService().getFrecuencia() : ServiceFrequency.EVENTUAL;
                task.setPeriodDate(calculatePeriodDate(newDateTime, freq));
            }
            if (task.getStatus() == TaskStatus.UNASSIGNED) {
                task.setStatus(TaskStatus.PENDING);
            }
            scheduledTaskRepository.save(task);
        });
    }

    public ScheduledTask assignSmart(Service service, LocalDateTime fechaProgramada) {
        ServiceFrequency freq = service.getFrecuencia() != null ? service.getFrecuencia() : ServiceFrequency.EVENTUAL;
        LocalDate periodDate = calculatePeriodDate(fechaProgramada, freq);

        List<ScheduledTask> unassigned = scheduledTaskRepository.findByServiceAndPeriodDateAndStatus(
                service, periodDate, TaskStatus.UNASSIGNED);

        if (!unassigned.isEmpty()) {
            ScheduledTask task = unassigned.get(0);
            task.setFechaProgramada(fechaProgramada);
            task.setStatus(TaskStatus.PENDING);
            return scheduledTaskRepository.save(task);
        }

        // If not found, we create a new eventual task for that service and date
        ScheduledTask newTask = ScheduledTask.builder()
                .service(service)
                .fechaProgramada(fechaProgramada)
                .periodDate(periodDate)
                .status(TaskStatus.PENDING)
                .type(TaskType.EVENTUAL)
                .build();
        return scheduledTaskRepository.save(newTask);
    }

    public LocalDate calculatePeriodDate(LocalDateTime date, ServiceFrequency freq) {
        LocalDate localDate = date.toLocalDate();
        if (freq == null) return localDate;
        
        return switch (freq) {
            case WEEKLY -> localDate.minusDays(localDate.getDayOfWeek().getValue() - 1);
            case FIFTEEN_DAYS -> localDate.getDayOfMonth() <= 15 
                    ? localDate.withDayOfMonth(1) 
                    : localDate.withDayOfMonth(16);
            case MONTHLY -> localDate.withDayOfMonth(1);
            default -> localDate;
        };
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> ids, TaskStatus status) {
        List<ScheduledTask> tasks = scheduledTaskRepository.findAllById(ids);
        for (ScheduledTask task : tasks) {
            task.setStatus(status);
        }
        scheduledTaskRepository.saveAll(tasks);
    }

    @Transactional
    public void markOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTask> pending = scheduledTaskRepository.findByStatus(TaskStatus.PENDING);
        for (ScheduledTask task : pending) {
            if (task.getFechaProgramada() != null && task.getFechaProgramada().isBefore(now)) {
                task.setStatus(TaskStatus.OVERDUE);
            }
        }
        scheduledTaskRepository.saveAll(pending);
    }
}
