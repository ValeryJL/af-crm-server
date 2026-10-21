package com.afcrm.server.controller;

import com.afcrm.server.dto.DashboardStatsDto;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus("ACTIVE");

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalUsersCount(totalUsers)
                .activeUsersCount(activeUsers)
                .availableTechniciansCount(activeUsers)
                .build();

        return ResponseEntity.ok(stats);
    }
}
