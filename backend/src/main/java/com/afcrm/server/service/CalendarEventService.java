package com.afcrm.server.service;

import com.afcrm.server.dto.CalendarEventDto;
import com.afcrm.server.dto.CalendarResponseDto;
import com.afcrm.server.dto.CalendarTaskDto;
import com.afcrm.server.model.CalendarEvent;
import com.afcrm.server.model.ScheduledTask;
import com.afcrm.server.repository.CalendarEventRepository;
import com.afcrm.server.repository.ScheduledTaskRepository;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarRepository;
    private final ScheduledTaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<CalendarEventDto> getAllEvents() {
        return calendarRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CalendarEventDto> getEventsInRange(LocalDateTime start, LocalDateTime end) {
        return calendarRepository.findOverlappingEvents(start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CalendarEventDto getEventById(UUID id) {
        return calendarRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public CalendarResponseDto getFullCalendarData(LocalDateTime start, LocalDateTime end) {
        List<CalendarEventDto> events = getEventsInRange(start, end);
        
        List<CalendarTaskDto> tasks = taskRepository.findByScheduledDateBetween(
                start.toLocalDate(), 
                end.toLocalDate()
        ).stream().map(this::mapTaskToDto).collect(Collectors.toList());

        return CalendarResponseDto.builder()
                .events(events)
                .tasks(tasks)
                .build();
    }


    @Transactional
    public CalendarEventDto createEvent(CalendarEventDto dto) {
        CalendarEvent event = CalendarEvent.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startTime(dto.getStart())
                .endTime(dto.getEnd())
                .allDay(dto.isAllDay())
                .location(dto.getLocation())
                .status(dto.getStatus())
                .color(dto.getColor())
                .user(dto.getUserId() != null ? userRepository.findById(dto.getUserId()).orElse(null) : null)
                .build();
        
        return mapToDto(calendarRepository.save(event));
    }

    @Transactional
    public CalendarEventDto updateEvent(UUID id, CalendarEventDto dto) {
        return calendarRepository.findById(id)
                .map(event -> {
                    event.setTitle(dto.getTitle());
                    event.setDescription(dto.getDescription());
                    event.setStartTime(dto.getStart());
                    event.setEndTime(dto.getEnd());
                    event.setAllDay(dto.isAllDay());
                    event.setLocation(dto.getLocation());
                    event.setStatus(dto.getStatus());
                    event.setColor(dto.getColor());
                    if (dto.getUserId() != null) {
                        event.setUser(userRepository.findById(dto.getUserId()).orElse(null));
                    }
                    return mapToDto(calendarRepository.save(event));
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Transactional
    public void deleteEvent(UUID id) {
        calendarRepository.deleteById(id);
    }

    private CalendarEventDto mapToDto(CalendarEvent event) {
        return CalendarEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .start(event.getStartTime())
                .end(event.getEndTime())
                .allDay(event.isAllDay())
                .location(event.getLocation())
                .status(event.getStatus())
                .color(event.getColor())
                .userId(event.getUser() != null ? event.getUser().getId() : null)
                .build();
    }

    private CalendarTaskDto mapTaskToDto(ScheduledTask task) {
        return CalendarTaskDto.builder()
                .id(task.getId())
                .scheduledDate(task.getScheduledDate())
                .status(task.getStatus())
                .type(task.getType())
                .serviceId(task.getService() != null ? task.getService().getId() : null)
                .serviceName(task.getService() != null ? task.getService().getNombre() : "Unknown Service")
                .build();
    }

}
