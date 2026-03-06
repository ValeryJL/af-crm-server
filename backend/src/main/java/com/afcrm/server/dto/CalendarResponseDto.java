package com.afcrm.server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarResponseDto {
    private List<CalendarEventDto> events;
    private List<CalendarTaskDto> tasks;
}
