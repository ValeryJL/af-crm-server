package com.afcrm.server.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EventualTaskRequest {
    private String descripcion;
    private LocalDate fecha;
    private Long serviceId;
}
