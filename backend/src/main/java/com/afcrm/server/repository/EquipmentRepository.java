package com.afcrm.server.repository;

import com.afcrm.server.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByServiceId(Long serviceId);
    List<Equipment> findByActivoTrue();
}

