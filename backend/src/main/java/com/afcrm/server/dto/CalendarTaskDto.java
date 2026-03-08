package com.afcrm.server.dto;

import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CalendarTaskDto {
    private Long id;
    private LocalDateTime fechaProgramada;
    private LocalDate periodDate;
    private String reportID;
    private TaskStatus status;
    private TaskType type;
    private Long serviceId;
    private String serviceName;
}
