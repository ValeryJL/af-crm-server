package com.afcrm.server.repository;

import com.afcrm.server.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByActivoTrueOrderByRazonSocialAsc();
}

