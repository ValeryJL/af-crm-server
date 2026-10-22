package com.afcrm.server.repository;

import com.afcrm.server.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByClientId(Long clientId);
    List<Service> findByActivoTrue();

    @Query("SELECT s FROM Service s WHERE s.activo = true AND s.fechaProximaVisitaLimite IS NOT NULL AND s.fechaProximaVisitaLimite < :date")
    List<Service> findOverdueServices(LocalDate date);

    @Query("SELECT s FROM Service s WHERE s.activo = true AND s.fechaProximaVisitaLimite IS NOT NULL AND s.fechaProximaVisitaLimite >= :startDate AND s.fechaProximaVisitaLimite <= :endDate")
    List<Service> findServicesDueBetween(LocalDate startDate, LocalDate endDate);
}

