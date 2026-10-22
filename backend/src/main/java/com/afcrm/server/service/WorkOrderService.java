package com.afcrm.server.service;

import com.afcrm.server.dto.WorkOrderDto;
import com.afcrm.server.model.*;
import com.afcrm.server.repository.EquipmentPartRepository;
import com.afcrm.server.repository.EquipmentRepository;
import com.afcrm.server.repository.ServiceRepository;
import com.afcrm.server.repository.UserRepository;
import com.afcrm.server.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final EquipmentPartRepository partRepository;
    private final EquipmentRepository equipmentRepository;
    private final ServiceEntityService serviceEntityService;

    @Transactional(readOnly = true)
    public List<WorkOrderDto> getAllWorkOrders() {
        return workOrderRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDto> getWorkOrdersByService(Long serviceId) {
        return workOrderRepository.findByServiceId(serviceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkOrderDto getWorkOrderById(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order no encontrada con ID: " + id));
        return toDto(wo);
    }

    @Transactional
    public WorkOrderDto createWorkOrder(WorkOrderDto dto) {
        Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + dto.getServiceId()));

        User tecnico = null;
        if (dto.getTecnicoId() != null) {
            tecnico = userRepository.findById(dto.getTecnicoId()).orElse(null);
        }

        String otNumber = dto.getNumeroOt();
        if (otNumber == null || otNumber.isBlank()) {
            otNumber = "OT-" + System.currentTimeMillis() % 1000000;
        }

        WorkOrderStatus estadoInicial = dto.getEstado();
        if (estadoInicial == null) {
            estadoInicial = (dto.getFechaProgramada() != null || tecnico != null) ? WorkOrderStatus.ASIGNADA : WorkOrderStatus.PENDIENTE;
        }

        WorkOrder wo = WorkOrder.builder()
                .numeroOt(otNumber)
                .tipo(dto.getTipo() != null ? dto.getTipo() : WorkOrderType.PREVENTIVO)
                .estado(estadoInicial)
                .fechaProgramada(dto.getFechaProgramada())
                .descripcion(dto.getDescripcion())
                .service(service)
                .tecnicoAsignado(tecnico)
                .formDataReporte(dto.getFormDataReporte() != null ? dto.getFormDataReporte() : Map.of())
                .partesReemplazadas(dto.getPartesReemplazadas() != null ? dto.getPartesReemplazadas() : Map.of())
                .build();

        return toDto(workOrderRepository.save(wo));
    }

    @Transactional
    public WorkOrderDto updateWorkOrder(Long id, WorkOrderDto dto) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order no encontrada con ID: " + id));

        if (dto.getTipo() != null) wo.setTipo(dto.getTipo());
        if (dto.getEstado() != null) wo.setEstado(dto.getEstado());
        wo.setFechaProgramada(dto.getFechaProgramada());
        if (dto.getDescripcion() != null) wo.setDescripcion(dto.getDescripcion());
        if (dto.getTecnicoId() != null) {
            User tecnico = userRepository.findById(dto.getTecnicoId()).orElse(null);
            wo.setTecnicoAsignado(tecnico);
        } else if (dto.getTecnicoId() == null && dto.getTecnicoNombre() == null) {
            // keep existing or leave null if explicitly desired
        }
        if (dto.getFormDataReporte() != null) wo.setFormDataReporte(dto.getFormDataReporte());
        if (dto.getPartesReemplazadas() != null) wo.setPartesReemplazadas(dto.getPartesReemplazadas());

        return toDto(workOrderRepository.save(wo));
    }

    @Transactional
    public WorkOrderDto updateSchedule(Long id, LocalDateTime fechaProgramada, Long tecnicoId) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order no encontrada con ID: " + id));

        wo.setFechaProgramada(fechaProgramada);
        if (tecnicoId != null) {
            User tecnico = userRepository.findById(tecnicoId).orElse(null);
            wo.setTecnicoAsignado(tecnico);
        }

        if (fechaProgramada != null && wo.getEstado() == WorkOrderStatus.PENDIENTE) {
            wo.setEstado(WorkOrderStatus.ASIGNADA);
        } else if (fechaProgramada == null && wo.getEstado() == WorkOrderStatus.ASIGNADA) {
            wo.setEstado(WorkOrderStatus.PENDIENTE);
        }

        return toDto(workOrderRepository.save(wo));
    }

    @Transactional
    public void deleteWorkOrder(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw new RuntimeException("Work Order no encontrada con ID: " + id);
        }
        workOrderRepository.deleteById(id);
    }

    @Transactional
    public WorkOrderDto updateStatus(Long id, WorkOrderStatus nuevoEstado) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order no encontrada con ID: " + id));

        wo.setEstado(nuevoEstado);
        if (nuevoEstado == WorkOrderStatus.EN_CURSO && wo.getFechaInicioReal() == null) {
            wo.setFechaInicioReal(LocalDateTime.now());
        }

        return toDto(workOrderRepository.save(wo));
    }

    @Transactional
    public WorkOrderDto completeWorkOrder(Long id, WorkOrderDto closeData) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order no encontrada con ID: " + id));

        wo.setEstado(WorkOrderStatus.COMPLETADA);
        wo.setFechaFinalizacion(LocalDateTime.now());

        if (closeData.getFormDataReporte() != null) {
            wo.setFormDataReporte(closeData.getFormDataReporte());
        }
        if (closeData.getPartesReemplazadas() != null) {
            wo.setPartesReemplazadas(closeData.getPartesReemplazadas());
        }
        if (closeData.getFirmaClienteNombre() != null) {
            wo.setFirmaClienteNombre(closeData.getFirmaClienteNombre());
        }
        if (closeData.getFirmaClienteDni() != null) {
            wo.setFirmaClienteDni(closeData.getFirmaClienteDni());
        }
        if (closeData.getObservacionesCierre() != null) {
            wo.setObservacionesCierre(closeData.getObservacionesCierre());
        }

        // Automatic SLA Recalculation on Parent Service
        Service service = wo.getService();
        if (service != null) {
            LocalDate today = LocalDate.now();
            service.setFechaUltimaVisita(today);
            service.setFechaProximaVisitaLimite(serviceEntityService.calculateNextVisit(today, service.getFrecuenciaVisita()));
            serviceRepository.save(service);
        }

        // Automatic update of replaced parts if present in partesReemplazadas
        if (wo.getPartesReemplazadas() != null && !wo.getPartesReemplazadas().isEmpty()) {
            for (Map.Entry<String, Object> entry : wo.getPartesReemplazadas().entrySet()) {
                try {
                    Long partId = Long.parseLong(entry.getKey());
                    partRepository.findById(partId).ifPresent(part -> {
                        part.setFechaUltimoCambio(LocalDate.now());
                        partRepository.save(part);
                    });
                } catch (Exception ignored) {
                }
            }
        }

        return toDto(workOrderRepository.save(wo));
    }

    public WorkOrderDto toDto(WorkOrder wo) {
        return WorkOrderDto.builder()
                .id(wo.getId())
                .numeroOt(wo.getNumeroOt())
                .tipo(wo.getTipo())
                .estado(wo.getEstado())
                .fechaProgramada(wo.getFechaProgramada())
                .fechaInicioReal(wo.getFechaInicioReal())
                .fechaFinalizacion(wo.getFechaFinalizacion())
                .descripcion(wo.getDescripcion())
                .serviceId(wo.getService() != null ? wo.getService().getId() : null)
                .serviceNombre(wo.getService() != null ? wo.getService().getNombre() : null)
                .clientRazonSocial(wo.getService() != null && wo.getService().getClient() != null ?
                        wo.getService().getClient().getRazonSocial() : null)
                .direccionFisica(wo.getService() != null ? wo.getService().getDireccionFisica() : null)
                .tecnicoId(wo.getTecnicoAsignado() != null ? wo.getTecnicoAsignado().getId() : null)
                .tecnicoNombre(wo.getTecnicoAsignado() != null ?
                        (wo.getTecnicoAsignado().getNombre() + " " + wo.getTecnicoAsignado().getApellido()).trim() : null)
                .formDataReporte(wo.getFormDataReporte())
                .partesReemplazadas(wo.getPartesReemplazadas())
                .firmaClienteNombre(wo.getFirmaClienteNombre())
                .firmaClienteDni(wo.getFirmaClienteDni())
                .observacionesCierre(wo.getObservacionesCierre())
                .build();
    }
}

