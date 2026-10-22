package com.afcrm.server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDto {
    private Long id;
    private String nombre;
    private String cargo;
    private String telefono;
    private String email;
    private String notas;
    private Long clientId;
    private Long serviceId;
}

