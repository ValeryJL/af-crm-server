package com.afcrm.server.repository;

import com.afcrm.server.model.TaskPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskPartRepository extends JpaRepository<TaskPart, Long> {
}
