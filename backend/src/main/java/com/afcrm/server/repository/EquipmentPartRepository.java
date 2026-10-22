package com.afcrm.server.repository;

import com.afcrm.server.model.EquipmentPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentPartRepository extends JpaRepository<EquipmentPart, Long> {
    List<EquipmentPart> findByEquipmentId(Long equipmentId);

    @Query("SELECT p FROM EquipmentPart p WHERE p.fechaUltimoCambio IS NOT NULL AND p.vidaUtilMeses IS NOT NULL")
    List<EquipmentPart> findAllWithLifespan();
}
