package com.afcrm.server.service;

import com.afcrm.server.dto.SlaAlertDto;
import com.afcrm.server.model.Equipment;
import com.afcrm.server.model.EquipmentPart;
import com.afcrm.server.model.Service;
import com.afcrm.server.model.ServiceFrequency;
import com.afcrm.server.repository.EquipmentPartRepository;
import com.afcrm.server.repository.EquipmentRepository;
import com.afcrm.server.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SlaAlertService {

    private final ServiceRepository serviceRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentPartRepository partRepository;
    private final ServiceEntityService serviceEntityService;

    @Transactional(readOnly = true)
    public List<SlaAlertDto> calculateAlerts() {
        List<SlaAlertDto> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Service Visit SLAs
        List<Service> services = serviceRepository.findByActivoTrue();
        for (Service s : services) {
            LocalDate nextVisit = s.getFechaProximaVisitaLimite();
            if (nextVisit == null && s.getFrecuenciaVisita() != null && s.getFrecuenciaVisita() != ServiceFrequency.A_DEMANDA) {
                nextVisit = serviceEntityService.calculateNextVisit(s.getFechaUltimaVisita(), s.getFrecuenciaVisita());
            }

            if (nextVisit != null) {
                long diff = ChronoUnit.DAYS.between(today, nextVisit);
                int avisoDias = s.getDiasAvisoAlerta() != null ? s.getDiasAvisoAlerta() : 7;

                if (diff < 0) {
                    // Critical Overdue
                    alerts.add(SlaAlertDto.builder()
                            .tipo("CRITICAL_OVERDUE")
                            .severidad("RED")
                            .titulo("Visita Vencida: " + s.getNombre())
                            .descripcion("El servicio tiene la visita preventiva vencida por " + Math.abs(diff) + " días.")
                            .clientId(s.getClient() != null ? s.getClient().getId() : null)
                            .clientRazonSocial(s.getClient() != null ? s.getClient().getRazonSocial() : null)
                            .serviceId(s.getId())
                            .serviceNombre(s.getNombre())
                            .fechaLimite(nextVisit)
                            .diasDiferencia(diff)
                            .accionSugerida("Agendar Visita Urgente")
                            .build());
                } else if (diff <= avisoDias) {
                    // Upcoming Due
                    alerts.add(SlaAlertDto.builder()
                            .tipo("UPCOMING_SLA")
                            .severidad("YELLOW")
                            .titulo("Próximo Service: " + s.getNombre())
                            .descripcion("Quedan " + diff + " días para cumplir la ventana de visita programada.")
                            .clientId(s.getClient() != null ? s.getClient().getId() : null)
                            .clientRazonSocial(s.getClient() != null ? s.getClient().getRazonSocial() : null)
                            .serviceId(s.getId())
                            .serviceNombre(s.getNombre())
                            .fechaLimite(nextVisit)
                            .diasDiferencia(diff)
                            .accionSugerida("Crear Orden de Trabajo")
                            .build());
                }
            }
        }

        // 2. Equipment Parts / Consumable Lifespan
        List<EquipmentPart> parts = partRepository.findAllWithLifespan();
        for (EquipmentPart part : parts) {
            LocalDate expDate = part.getFechaUltimoCambio().plusMonths(part.getVidaUtilMeses());
            long diff = ChronoUnit.DAYS.between(today, expDate);
            int aviso = part.getAlertaTempranaDias() != null ? part.getAlertaTempranaDias() : 15;

            if (diff <= aviso) {
                String desc = diff < 0 ?
                        "Parte vencida hace " + Math.abs(diff) + " días." :
                        "Consumible por vencer en " + diff + " días.";

                Equipment eq = part.getEquipment();
                Service s = eq != null ? eq.getService() : null;

                alerts.add(SlaAlertDto.builder()
                        .tipo("CONSUMABLE_EXPIRED")
                        .severidad("ORANGE")
                        .titulo("Consumible Crítico: " + part.getNombreParte())
                        .descripcion(desc + " (Equipo: " + (eq != null ? eq.getNombre() : "N/A") + ")")
                        .clientId(s != null && s.getClient() != null ? s.getClient().getId() : null)
                        .clientRazonSocial(s != null && s.getClient() != null ? s.getClient().getRazonSocial() : null)
                        .serviceId(s != null ? s.getId() : null)
                        .serviceNombre(s != null ? s.getNombre() : null)
                        .equipmentId(eq != null ? eq.getId() : null)
                        .equipmentNombre(eq != null ? eq.getNombre() : null)
                        .fechaLimite(expDate)
                        .diasDiferencia(diff)
                        .accionSugerida("Solicitar Repuesto / Reemplazo")
                        .build());
            }
        }

        return alerts;
    }
}

