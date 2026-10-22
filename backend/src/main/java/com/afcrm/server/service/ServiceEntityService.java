package com.afcrm.server.service;

import com.afcrm.server.dto.EquipmentDto;
import com.afcrm.server.dto.EquipmentPartDto;
import com.afcrm.server.dto.ServiceDto;
import com.afcrm.server.model.Client;
import com.afcrm.server.model.Equipment;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.ServiceFrequency;
import com.afcrm.server.repository.ClientRepository;
import com.afcrm.server.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getServicesByClient(Long clientId) {
        return serviceRepository.findByClientId(clientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceDto getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        return toDto(service);
    }

    @Transactional
    public ServiceDto createService(ServiceDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClientId()));

        Service service = Service.builder()
                .nombre(dto.getNombre())
                .direccionFisica(dto.getDireccionFisica())
                .localidad(dto.getLocalidad())
                .provincia(dto.getProvincia())
                .frecuenciaVisita(dto.getFrecuenciaVisita() != null ? dto.getFrecuenciaVisita() : ServiceFrequency.MENSUAL)
                .diasAvisoAlerta(dto.getDiasAvisoAlerta() != null ? dto.getDiasAvisoAlerta() : 7)
                .fechaUltimaVisita(dto.getFechaUltimaVisita())
                .fechaProximaVisitaLimite(calculateNextVisit(dto.getFechaUltimaVisita(), dto.getFrecuenciaVisita()))
                .notas(dto.getNotas())
                .activo(true)
                .client(client)
                .build();

        return toDto(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto updateService(Long id, ServiceDto dto) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));

        service.setNombre(dto.getNombre());
        service.setDireccionFisica(dto.getDireccionFisica());
        service.setLocalidad(dto.getLocalidad());
        service.setProvincia(dto.getProvincia());
        service.setFrecuenciaVisita(dto.getFrecuenciaVisita());
        service.setDiasAvisoAlerta(dto.getDiasAvisoAlerta());
        service.setNotas(dto.getNotas());
        if (dto.getActivo() != null) {
            service.setActivo(dto.getActivo());
        }

        if (dto.getFechaUltimaVisita() != null && !dto.getFechaUltimaVisita().equals(service.getFechaUltimaVisita())) {
            service.setFechaUltimaVisita(dto.getFechaUltimaVisita());
            service.setFechaProximaVisitaLimite(calculateNextVisit(dto.getFechaUltimaVisita(), dto.getFrecuenciaVisita()));
        } else if (dto.getFechaProximaVisitaLimite() != null) {
            service.setFechaProximaVisitaLimite(dto.getFechaProximaVisitaLimite());
        }

        return toDto(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        service.setActivo(false);
        serviceRepository.save(service);
    }

    public LocalDate calculateNextVisit(LocalDate lastVisit, ServiceFrequency frequency) {
        if (lastVisit == null || frequency == null || frequency == ServiceFrequency.A_DEMANDA) {
            return null;
        }
        return switch (frequency) {
            case SEMANAL -> lastVisit.plusWeeks(1);
            case QUINCENAL -> lastVisit.plusWeeks(2);
            case MENSUAL -> lastVisit.plusMonths(1);
            case BIMESTRAL -> lastVisit.plusMonths(2);
            case TRIMESTRAL -> lastVisit.plusMonths(3);
            case SEMESTRAL -> lastVisit.plusMonths(6);
            case ANUAL -> lastVisit.plusYears(1);
            default -> null;
        };
    }

    public ServiceDto toDto(Service s) {
        LocalDate today = LocalDate.now();
        String slaStatus = "ON_TIME";
        Long diasParaVisita = null;

        if (s.getFechaProximaVisitaLimite() != null) {
            diasParaVisita = ChronoUnit.DAYS.between(today, s.getFechaProximaVisitaLimite());
            if (diasParaVisita < 0) {
                slaStatus = "OVERDUE";
            } else if (diasParaVisita <= (s.getDiasAvisoAlerta() != null ? s.getDiasAvisoAlerta() : 7)) {
                slaStatus = "UPCOMING";
            }
        }

        List<EquipmentDto> equipments = s.getEquipments() != null ?
                s.getEquipments().stream().map(e -> EquipmentDto.builder()
                        .id(e.getId())
                        .nombre(e.getNombre())
                        .marca(e.getMarca())
                        .modelo(e.getModelo())
                        .numeroSerie(e.getNumeroSerie())
                        .ubicacion(e.getUbicacion())
                        .horometroActual(e.getHorometroActual())
                        .fechaProximoServiceMayor(e.getFechaProximoServiceMayor())
                        .specs(e.getSpecs())
                        .activo(e.isActivo())
                        .serviceId(s.getId())
                        .build()
                ).collect(Collectors.toList()) : List.of();

        return ServiceDto.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .direccionFisica(s.getDireccionFisica())
                .localidad(s.getLocalidad())
                .provincia(s.getProvincia())
                .frecuenciaVisita(s.getFrecuenciaVisita())
                .diasAvisoAlerta(s.getDiasAvisoAlerta())
                .fechaUltimaVisita(s.getFechaUltimaVisita())
                .fechaProximaVisitaLimite(s.getFechaProximaVisitaLimite())
                .notas(s.getNotas())
                .activo(s.isActivo())
                .clientId(s.getClient() != null ? s.getClient().getId() : null)
                .clientRazonSocial(s.getClient() != null ? s.getClient().getRazonSocial() : null)
                .totalEquipos(s.getEquipments() != null ? s.getEquipments().size() : 0)
                .slaStatus(slaStatus)
                .diasParaVisita(diasParaVisita)
                .equipments(equipments)
                .build();
    }
}

