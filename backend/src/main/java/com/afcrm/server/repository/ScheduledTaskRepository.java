package com.afcrm.server.repository;

import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    List<ScheduledTask> findByServiceAndScheduledDateAfter(Service service, java.time.LocalDate date);
    List<ScheduledTask> findByScheduledDateBetween(java.time.LocalDate start, java.time.LocalDate end);
}
