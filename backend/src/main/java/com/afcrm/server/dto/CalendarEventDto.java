package com.afcrm.server.dto;

import com.afcrm.server.model.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CalendarEventDto {
    private java.util.UUID id;
    private String title;
    private String description;
    private java.time.LocalDateTime start;
    private java.time.LocalDateTime end;
    private boolean allDay;
    private String location;
    private TaskStatus status;
    private String color;
    private Long userId;
}
