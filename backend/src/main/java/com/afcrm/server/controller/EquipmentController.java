package com.afcrm.server.controller;

import com.afcrm.server.dto.EquipmentDto;
import com.afcrm.server.dto.EquipmentPartDto;
import com.afcrm.server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<List<EquipmentDto>> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<EquipmentDto>> getEquipmentsByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(equipmentService.getEquipmentsByService(serviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDto> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @PostMapping
    public ResponseEntity<EquipmentDto> createEquipment(@RequestBody EquipmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDto> updateEquipment(@PathVariable Long id, @RequestBody EquipmentDto dto) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    // Part Sub-endpoints
    @PostMapping("/{id}/parts")
    public ResponseEntity<EquipmentPartDto> addPart(@PathVariable Long id, @RequestBody EquipmentPartDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.addPart(id, dto));
    }

    @PutMapping("/parts/{partId}")
    public ResponseEntity<EquipmentPartDto> updatePart(@PathVariable Long partId, @RequestBody EquipmentPartDto dto) {
        return ResponseEntity.ok(equipmentService.updatePart(partId, dto));
    }

    @DeleteMapping("/parts/{partId}")
    public ResponseEntity<Void> deletePart(@PathVariable Long partId) {
        equipmentService.deletePart(partId);
        return ResponseEntity.noContent().build();
    }
}

