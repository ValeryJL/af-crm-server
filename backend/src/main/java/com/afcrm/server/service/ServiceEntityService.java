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
import java.time.temporal.TemporalAdjusters;
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
        if (frequency == null || frequency == ServiceFrequency.A_DEMANDA) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (lastVisit == null) {
            return switch (frequency) {
                case SEMANAL -> today.plusDays(3);
                case QUINCENAL -> today.plusDays(7);
                case MENSUAL -> today.withDayOfMonth(Math.min(28, today.lengthOfMonth()));
                case BIMESTRAL -> today.plusMonths(1);
                case TRIMESTRAL -> today.plusMonths(1);
                case SEMESTRAL -> today.plusMonths(2);
                case ANUAL -> today.plusMonths(3);
                default -> today.plusDays(7);
            };
        }

        switch (frequency) {
            case MENSUAL -> {
                int targetDay = Math.min(lastVisit.getDayOfMonth(), today.lengthOfMonth());
                LocalDate targetThisMonth = today.withDayOfMonth(targetDay);
                // Si ya fue visitado en este mes calendario (en o después del día objetivo), programar para el mes siguiente
                if (lastVisit.getYear() == today.getYear() && lastVisit.getMonth() == today.getMonth() && !lastVisit.isBefore(targetThisMonth)) {
                    LocalDate nextMonth = today.plusMonths(1);
                    return nextMonth.withDayOfMonth(Math.min(targetDay, nextMonth.lengthOfMonth()));
                }
                return targetThisMonth;
            }
            case SEMANAL -> {
                if (lastVisit.isAfter(today.minusDays(6))) {
                    return lastVisit.plusWeeks(1);
                }
                LocalDate targetThisWeek = today.with(TemporalAdjusters.previousOrSame(lastVisit.getDayOfWeek()));
                if (targetThisWeek.isBefore(today.minusDays(3))) {
                    targetThisWeek = today.with(TemporalAdjusters.nextOrSame(lastVisit.getDayOfWeek()));
                }
                return targetThisWeek;
            }
            case QUINCENAL -> {
                if (lastVisit.isAfter(today.minusDays(13))) {
                    return lastVisit.plusWeeks(2);
                }
                int dayOfMonth = today.getDayOfMonth();
                int day1 = Math.min(Math.min(lastVisit.getDayOfMonth(), 15), today.lengthOfMonth());
                int day2 = Math.min(day1 + 14, today.lengthOfMonth());
                if (dayOfMonth <= day1) {
                    return today.withDayOfMonth(day1);
                } else if (dayOfMonth <= day2) {
                    return today.withDayOfMonth(day2);
                } else {
                    LocalDate nextMonth = today.plusMonths(1);
                    return nextMonth.withDayOfMonth(Math.min(day1, nextMonth.lengthOfMonth()));
                }
            }
            case BIMESTRAL -> {
                return lastVisit.plusMonths(2);
            }
            case TRIMESTRAL -> {
                return lastVisit.plusMonths(3);
            }
            case SEMESTRAL -> {
                return lastVisit.plusMonths(6);
            }
            case ANUAL -> {
                return lastVisit.plusYears(1);
            }
            default -> {
                return lastVisit.plusMonths(1);
            }
        }
    }

    public ServiceDto toDto(Service s) {
        LocalDate today = LocalDate.now();
        String slaStatus = "ON_TIME";
        Long diasParaVisita = null;

        LocalDate nextVisit = s.getFechaProximaVisitaLimite();
        if (nextVisit == null && s.getFrecuenciaVisita() != null && s.getFrecuenciaVisita() != ServiceFrequency.A_DEMANDA) {
            nextVisit = calculateNextVisit(s.getFechaUltimaVisita(), s.getFrecuenciaVisita());
        }

        if (s.getFrecuenciaVisita() == ServiceFrequency.A_DEMANDA) {
            slaStatus = "A_DEMANDA";
        } else if (nextVisit != null) {
            diasParaVisita = ChronoUnit.DAYS.between(today, nextVisit);
            if (diasParaVisita < 0) {
                slaStatus = "OVERDUE";
            } else if (diasParaVisita <= (s.getDiasAvisoAlerta() != null ? s.getDiasAvisoAlerta() : 7)) {
                slaStatus = "UPCOMING";
            } else {
                slaStatus = "ON_TIME";
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
                .fechaProximaVisitaLimite(nextVisit)
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

