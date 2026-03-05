package com.afcrm.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long activeServicesCount;
    private long availableTechniciansCount;
    private long pendingTasksCount;
}
