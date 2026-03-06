package com.afcrm.server.repository;

import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import jakarta.transaction.Transactional;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    List<ScheduledTask> findByServiceAndScheduledDateAfterAndStatus(Service service, java.time.LocalDate date, com.afcrm.server.model.TaskStatus status);
    List<ScheduledTask> findByScheduledDateBetween(java.time.LocalDate start, java.time.LocalDate end);
    long countByStatus(com.afcrm.server.model.TaskStatus status);

    @Transactional
    void deleteByServiceAndScheduledDateAfterAndStatus(Service service, java.time.LocalDate date, com.afcrm.server.model.TaskStatus status);

    @Transactional
    void deleteByService(Service service);
}
