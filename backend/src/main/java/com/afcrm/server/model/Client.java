package com.afcrm.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "cuit_rut")
    private String cuitRut;

    @Column(name = "condicion_fiscal")
    private String condicionFiscal;

    @Column(name = "direccion_fiscal")
    private String direccionFiscal;

    @Column(name = "telefono_general")
    private String telefonoGeneral;

    @Column(name = "email_comercial")
    private String emailComercial;

    @Column(columnDefinition = "text")
    private String notas;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Service> services = new ArrayList<>();
}
