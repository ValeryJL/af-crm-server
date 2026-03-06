package com.afcrm.server.controller;

import com.afcrm.server.dto.CalendarEventDto;
import com.afcrm.server.dto.CalendarResponseDto;
import com.afcrm.server.service.CalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping
    public List<CalendarEventDto> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (start != null && end != null) {
            return calendarEventService.getEventsInRange(start, end);
        }
        return calendarEventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEventDto> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(calendarEventService.getEventById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public CalendarResponseDto getFullCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return calendarEventService.getFullCalendarData(start, end);
    }

    @PostMapping
    public CalendarEventDto createEvent(@RequestBody CalendarEventDto dto) {
        return calendarEventService.createEvent(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventDto> updateEvent(@PathVariable UUID id, @RequestBody CalendarEventDto dto) {
        try {
            return ResponseEntity.ok(calendarEventService.updateEvent(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
