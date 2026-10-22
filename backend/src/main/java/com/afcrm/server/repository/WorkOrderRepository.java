package com.afcrm.server.repository;

import com.afcrm.server.model.WorkOrder;
import com.afcrm.server.model.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByServiceId(Long serviceId);
    List<WorkOrder> findByEstado(WorkOrderStatus estado);
    List<WorkOrder> findByTecnicoAsignadoId(Long tecnicoId);
    Optional<WorkOrder> findByNumeroOt(String numeroOt);
    long countByEstado(WorkOrderStatus estado);
}

