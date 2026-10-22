package com.afcrm.server.service;

import com.afcrm.server.dto.ContactDto;
import com.afcrm.server.model.Client;
import com.afcrm.server.model.Contact;
import com.afcrm.server.model.Service;
import com.afcrm.server.repository.ClientRepository;
import com.afcrm.server.repository.ContactRepository;
import com.afcrm.server.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ContactDto> getContactsByClient(Long clientId) {
        return contactRepository.findByClientId(clientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContactDto createContact(ContactDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClientId()));

        Service service = null;
        if (dto.getServiceId() != null) {
            service = serviceRepository.findById(dto.getServiceId()).orElse(null);
        }

        Contact contact = Contact.builder()
                .nombre(dto.getNombre())
                .cargo(dto.getCargo())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .notas(dto.getNotas())
                .client(client)
                .service(service)
                .build();

        return toDto(contactRepository.save(contact));
    }

    @Transactional
    public ContactDto updateContact(Long id, ContactDto dto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + id));

        contact.setNombre(dto.getNombre());
        contact.setCargo(dto.getCargo());
        contact.setTelefono(dto.getTelefono());
        contact.setEmail(dto.getEmail());
        contact.setNotas(dto.getNotas());

        if (dto.getServiceId() != null) {
            Service service = serviceRepository.findById(dto.getServiceId()).orElse(null);
            contact.setService(service);
        } else {
            contact.setService(null);
        }

        return toDto(contactRepository.save(contact));
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

    public ContactDto toDto(Contact contact) {
        return ContactDto.builder()
                .id(contact.getId())
                .nombre(contact.getNombre())
                .cargo(contact.getCargo())
                .telefono(contact.getTelefono())
                .email(contact.getEmail())
                .notas(contact.getNotas())
                .clientId(contact.getClient() != null ? contact.getClient().getId() : null)
                .serviceId(contact.getService() != null ? contact.getService().getId() : null)
                .build();
    }
}

