package com.afcrm.server.controller;

import com.afcrm.server.dto.SlaAlertDto;
import com.afcrm.server.service.SlaAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final SlaAlertService slaAlertService;

    @GetMapping
    public ResponseEntity<List<SlaAlertDto>> getSlaAlerts() {
        return ResponseEntity.ok(slaAlertService.calculateAlerts());
    }
}

