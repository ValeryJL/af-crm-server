package com.afcrm.server.repository;

import com.afcrm.server.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    long countByBajaIsNull();
}
