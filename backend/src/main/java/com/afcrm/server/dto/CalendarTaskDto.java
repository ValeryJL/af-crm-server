package com.afcrm.server.dto;

import com.afcrm.server.model.TaskStatus;
import com.afcrm.server.model.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CalendarTaskDto {
    private Long id;
    private LocalDate scheduledDate;
    private TaskStatus status;
    private TaskType type;
    private Long serviceId;
    private String serviceName;
}
