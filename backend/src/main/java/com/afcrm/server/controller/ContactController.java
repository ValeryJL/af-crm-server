package com.afcrm.server.controller;

import com.afcrm.server.dto.ContactDto;
import com.afcrm.server.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContactDto>> getContactsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contactService.getContactsByClient(clientId));
    }

    @PostMapping
    public ResponseEntity<ContactDto> createContact(@RequestBody ContactDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createContact(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDto> updateContact(@PathVariable Long id, @RequestBody ContactDto dto) {
        return ResponseEntity.ok(contactService.updateContact(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}

