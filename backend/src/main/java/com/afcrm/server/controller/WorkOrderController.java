package com.afcrm.server.controller;

import com.afcrm.server.dto.WorkOrderDto;
import com.afcrm.server.model.WorkOrderStatus;
import com.afcrm.server.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ResponseEntity<List<WorkOrderDto>> getAllWorkOrders() {
        return ResponseEntity.ok(workOrderService.getAllWorkOrders());
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<WorkOrderDto>> getWorkOrdersByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByService(serviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDto> getWorkOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getWorkOrderById(id));
    }

    @PostMapping
    public ResponseEntity<WorkOrderDto> createWorkOrder(@RequestBody WorkOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workOrderService.createWorkOrder(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkOrderDto> updateStatus(@PathVariable Long id, @RequestParam WorkOrderStatus status) {
        return ResponseEntity.ok(workOrderService.updateStatus(id, status));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<WorkOrderDto> completeWorkOrder(@PathVariable Long id, @RequestBody WorkOrderDto closeData) {
        return ResponseEntity.ok(workOrderService.completeWorkOrder(id, closeData));
    }
}

