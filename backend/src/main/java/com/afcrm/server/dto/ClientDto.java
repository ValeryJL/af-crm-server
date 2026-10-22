package com.afcrm.server.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {
    private Long id;
    private String razonSocial;
    private String cuitRut;
    private String condicionFiscal;
    private String direccionFiscal;
    private String telefonoGeneral;
    private String emailComercial;
    private String notas;
    private Boolean activo;
    private Integer totalServicios;
    private Integer totalEquipos;
    private List<ContactDto> contacts;
}

