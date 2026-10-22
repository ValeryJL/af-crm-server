package com.afcrm.server.service;

import com.afcrm.server.dto.ClientDto;
import com.afcrm.server.dto.ContactDto;
import com.afcrm.server.model.Client;
import com.afcrm.server.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientDto getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return toDto(client);
    }

    @Transactional
    public ClientDto createClient(ClientDto dto) {
        Client client = Client.builder()
                .razonSocial(dto.getRazonSocial())
                .cuitRut(dto.getCuitRut())
                .condicionFiscal(dto.getCondicionFiscal())
                .direccionFiscal(dto.getDireccionFiscal())
                .telefonoGeneral(dto.getTelefonoGeneral())
                .emailComercial(dto.getEmailComercial())
                .notas(dto.getNotas())
                .activo(true)
                .build();
        return toDto(clientRepository.save(client));
    }

    @Transactional
    public ClientDto updateClient(Long id, ClientDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        client.setRazonSocial(dto.getRazonSocial());
        client.setCuitRut(dto.getCuitRut());
        client.setCondicionFiscal(dto.getCondicionFiscal());
        client.setDireccionFiscal(dto.getDireccionFiscal());
        client.setTelefonoGeneral(dto.getTelefonoGeneral());
        client.setEmailComercial(dto.getEmailComercial());
        client.setNotas(dto.getNotas());
        if (dto.getActivo() != null && dto.getActivo() != client.isActivo()) {
            client.setActivo(dto.getActivo());
        }

        return toDto(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        client.setActivo(false);
        clientRepository.save(client);
    }

    public ClientDto toDto(Client client) {
        int totalServicios = client.getServices() != null ? client.getServices().size() : 0;
        int totalEquipos = client.getServices() != null ?
                client.getServices().stream().mapToInt(s -> s.getEquipments() != null ? s.getEquipments().size() : 0).sum() : 0;

        List<ContactDto> contacts = client.getContacts() != null ?
                client.getContacts().stream().map(c -> ContactDto.builder()
                        .id(c.getId())
                        .nombre(c.getNombre())
                        .cargo(c.getCargo())
                        .telefono(c.getTelefono())
                        .email(c.getEmail())
                        .notas(c.getNotas())
                        .clientId(client.getId())
                        .serviceId(c.getService() != null ? c.getService().getId() : null)
                        .build()
                ).collect(Collectors.toList()) : List.of();

        return ClientDto.builder()
                .id(client.getId())
                .razonSocial(client.getRazonSocial())
                .cuitRut(client.getCuitRut())
                .condicionFiscal(client.getCondicionFiscal())
                .direccionFiscal(client.getDireccionFiscal())
                .telefonoGeneral(client.getTelefonoGeneral())
                .emailComercial(client.getEmailComercial())
                .notas(client.getNotas())
                .activo(client.isActivo())
                .totalServicios(totalServicios)
                .totalEquipos(totalEquipos)
                .contacts(contacts)
                .build();
    }
}

