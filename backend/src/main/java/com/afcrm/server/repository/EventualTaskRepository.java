package com.afcrm.server.repository;

import com.afcrm.server.model.EventualTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventualTaskRepository extends JpaRepository<EventualTask, Long> {
    long countByStatus(com.afcrm.server.model.TaskStatus status);
}
