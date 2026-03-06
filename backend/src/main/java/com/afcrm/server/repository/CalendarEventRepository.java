package com.afcrm.server.repository;

import com.afcrm.server.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {
    
    List<CalendarEvent> findByUserId(Long userId);

    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime < :end AND e.endTime > :start")
    List<CalendarEvent> findOverlappingEvents(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<CalendarEvent> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CalendarEvent> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

}
