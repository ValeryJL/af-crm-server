package com.afcrm.server.service;

import com.afcrm.server.dto.EquipmentDto;
import com.afcrm.server.dto.EquipmentPartDto;
import com.afcrm.server.model.Equipment;
import com.afcrm.server.model.EquipmentPart;
import com.afcrm.server.model.Service;
import com.afcrm.server.repository.EquipmentPartRepository;
import com.afcrm.server.repository.EquipmentRepository;
import com.afcrm.server.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentPartRepository partRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<EquipmentDto> getEquipmentsByService(Long serviceId) {
        return equipmentRepository.findByServiceId(serviceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentDto> getAllEquipments() {
        return equipmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentDto getEquipmentById(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + id));
        return toDto(eq);
    }

    @Transactional
    public EquipmentDto createEquipment(EquipmentDto dto) {
        Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + dto.getServiceId()));

        Equipment eq = Equipment.builder()
                .nombre(dto.getNombre())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .numeroSerie(dto.getNumeroSerie())
                .ubicacion(dto.getUbicacion())
                .horometroActual(dto.getHorometroActual())
                .periodicidadRegular(dto.getPeriodicidadRegular())
                .intervaloHorasRegular(dto.getIntervaloHorasRegular())
                .fechaUltimaVisitaRegular(dto.getFechaUltimaVisitaRegular())
                .fechaProximaVisitaRegular(dto.getFechaProximaVisitaRegular())
                .periodicidadServiceMayor(dto.getPeriodicidadServiceMayor())
                .fechaUltimoServiceMayor(dto.getFechaUltimoServiceMayor())
                .fechaProximoServiceMayor(dto.getFechaProximoServiceMayor())
                .intervaloHorasService(dto.getIntervaloHorasService())
                .specs(dto.getSpecs() != null ? dto.getSpecs() : java.util.Map.of())
                .notas(dto.getNotas())
                .activo(true)
                .service(service)
                .build();

        return toDto(equipmentRepository.save(eq));
    }

    @Transactional
    public EquipmentDto updateEquipment(Long id, EquipmentDto dto) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + id));

        eq.setNombre(dto.getNombre());
        eq.setMarca(dto.getMarca());
        eq.setModelo(dto.getModelo());
        eq.setNumeroSerie(dto.getNumeroSerie());
        eq.setUbicacion(dto.getUbicacion());
        eq.setHorometroActual(dto.getHorometroActual());
        eq.setPeriodicidadRegular(dto.getPeriodicidadRegular());
        eq.setIntervaloHorasRegular(dto.getIntervaloHorasRegular());
        eq.setFechaUltimaVisitaRegular(dto.getFechaUltimaVisitaRegular());
        eq.setFechaProximaVisitaRegular(dto.getFechaProximaVisitaRegular());
        eq.setPeriodicidadServiceMayor(dto.getPeriodicidadServiceMayor());
        eq.setFechaUltimoServiceMayor(dto.getFechaUltimoServiceMayor());
        eq.setFechaProximoServiceMayor(dto.getFechaProximoServiceMayor());
        eq.setIntervaloHorasService(dto.getIntervaloHorasService());
        if (dto.getSpecs() != null) {
            eq.setSpecs(dto.getSpecs());
        }
        eq.setNotas(dto.getNotas());
        if (dto.getActivo() != null) {
            eq.setActivo(dto.getActivo());
        }

        return toDto(equipmentRepository.save(eq));
    }

    @Transactional
    public void deleteEquipment(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + id));
        eq.setActivo(false);
        equipmentRepository.save(eq);
    }

    // Part management
    @Transactional
    public EquipmentPartDto addPart(Long equipmentId, EquipmentPartDto dto) {
        Equipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + equipmentId));

        EquipmentPart part = EquipmentPart.builder()
                .nombreParte(dto.getNombreParte())
                .codigoRepuesto(dto.getCodigoRepuesto())
                .fechaUltimoCambio(dto.getFechaUltimoCambio() != null ? dto.getFechaUltimoCambio() : LocalDate.now())
                .vidaUtilMeses(dto.getVidaUtilMeses())
                .vidaUtilHoras(dto.getVidaUtilHoras())
                .alertaTempranaDias(dto.getAlertaTempranaDias() != null ? dto.getAlertaTempranaDias() : 15)
                .notas(dto.getNotas())
                .equipment(eq)
                .build();

        return toPartDto(partRepository.save(part));
    }

    @Transactional
    public EquipmentPartDto updatePart(Long partId, EquipmentPartDto dto) {
        EquipmentPart part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Parte no encontrada con ID: " + partId));

        part.setNombreParte(dto.getNombreParte());
        part.setCodigoRepuesto(dto.getCodigoRepuesto());
        part.setFechaUltimoCambio(dto.getFechaUltimoCambio());
        part.setVidaUtilMeses(dto.getVidaUtilMeses());
        part.setVidaUtilHoras(dto.getVidaUtilHoras());
        part.setAlertaTempranaDias(dto.getAlertaTempranaDias());
        part.setNotas(dto.getNotas());

        return toPartDto(partRepository.save(part));
    }

    @Transactional
    public void deletePart(Long partId) {
        partRepository.deleteById(partId);
    }

    public EquipmentDto toDto(Equipment eq) {
        List<EquipmentPartDto> parts = eq.getParts() != null ?
                eq.getParts().stream().map(this::toPartDto).collect(Collectors.toList()) : List.of();

        return EquipmentDto.builder()
                .id(eq.getId())
                .nombre(eq.getNombre())
                .marca(eq.getMarca())
                .modelo(eq.getModelo())
                .numeroSerie(eq.getNumeroSerie())
                .ubicacion(eq.getUbicacion())
                .horometroActual(eq.getHorometroActual())
                .periodicidadRegular(eq.getPeriodicidadRegular())
                .intervaloHorasRegular(eq.getIntervaloHorasRegular())
                .fechaUltimaVisitaRegular(eq.getFechaUltimaVisitaRegular())
                .fechaProximaVisitaRegular(eq.getFechaProximaVisitaRegular())
                .periodicidadServiceMayor(eq.getPeriodicidadServiceMayor())
                .fechaUltimoServiceMayor(eq.getFechaUltimoServiceMayor())
                .fechaProximoServiceMayor(eq.getFechaProximoServiceMayor())
                .intervaloHorasService(eq.getIntervaloHorasService())
                .specs(eq.getSpecs())
                .notas(eq.getNotas())
                .activo(eq.isActivo())
                .serviceId(eq.getService() != null ? eq.getService().getId() : null)
                .serviceNombre(eq.getService() != null ? eq.getService().getNombre() : null)
                .clientRazonSocial(eq.getService() != null && eq.getService().getClient() != null ?
                        eq.getService().getClient().getRazonSocial() : null)
                .parts(parts)
                .build();
    }

    public EquipmentPartDto toPartDto(EquipmentPart p) {
        LocalDate expDate = null;
        boolean expired = false;
        if (p.getFechaUltimoCambio() != null && p.getVidaUtilMeses() != null) {
            expDate = p.getFechaUltimoCambio().plusMonths(p.getVidaUtilMeses());
            if (LocalDate.now().isAfter(expDate)) {
                expired = true;
            }
        }

        return EquipmentPartDto.builder()
                .id(p.getId())
                .nombreParte(p.getNombreParte())
                .codigoRepuesto(p.getCodigoRepuesto())
                .fechaUltimoCambio(p.getFechaUltimoCambio())
                .vidaUtilMeses(p.getVidaUtilMeses())
                .vidaUtilHoras(p.getVidaUtilHoras())
                .alertaTempranaDias(p.getAlertaTempranaDias())
                .notas(p.getNotas())
                .equipmentId(p.getEquipment() != null ? p.getEquipment().getId() : null)
                .equipmentNombre(p.getEquipment() != null ? p.getEquipment().getNombre() : null)
                .fechaVencimientoEstimada(expDate)
                .vencido(expired)
                .build();
    }
}

