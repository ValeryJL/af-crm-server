package com.afcrm.server.repository;

import com.afcrm.server.model.ServiceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceReportRepository extends JpaRepository<ServiceReport, Long> {
}
