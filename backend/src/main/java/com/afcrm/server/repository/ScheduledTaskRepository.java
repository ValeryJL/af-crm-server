package com.afcrm.server.repository;

import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    @Query("SELECT t FROM ScheduledTask t WHERE t.service = :service "
        + "AND (:hasStatus = false OR t.status = :status) "
        + "AND (:hasType = false OR t.type = :type) "
        + "AND (:hasFrom = false OR t.fechaProgramada >= :from) "
        + "AND (:hasTo = false OR t.fechaProgramada <= :to) "
        + "ORDER BY CASE WHEN t.fechaProgramada IS NULL THEN 1 ELSE 0 END, t.fechaProgramada ASC")
    List<ScheduledTask> findTasksByService(
        @Param("service") Service service,
        @Param("hasStatus") boolean hasStatus,
        @Param("status") TaskStatus status,
        @Param("hasType") boolean hasType,
        @Param("type") TaskType type,
        @Param("hasFrom") boolean hasFrom,
        @Param("from") LocalDateTime from,
        @Param("hasTo") boolean hasTo,
        @Param("to") LocalDateTime to
    );

    List<ScheduledTask> findByServiceAndFechaProgramadaAfterAndStatus(Service service, LocalDateTime date, TaskStatus status);
    List<ScheduledTask> findByFechaProgramadaBetween(LocalDateTime start, LocalDateTime end);
    List<ScheduledTask> findByStatusAndPeriodDateBetween(TaskStatus status, LocalDate start, LocalDate end);
    List<ScheduledTask> findByStatus(TaskStatus status);
    List<ScheduledTask> findByServiceAndPeriodDateAndStatus(Service service, LocalDate periodDate, TaskStatus status);
    long countByStatus(TaskStatus status);

    @Transactional
    void deleteByServiceAndFechaProgramadaAfterAndStatus(Service service, LocalDateTime date, TaskStatus status);

    @Transactional
    void deleteByServiceAndStatus(Service service, TaskStatus status);

    @Transactional
    void deleteByService(Service service);
}
