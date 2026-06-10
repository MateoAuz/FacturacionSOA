package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sucursal")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "nombre", nullable = false, length = 40)
    private String nombre;

    @Column(name = "ciudad", nullable = false, length = 30)
    private String ciudad;

    @Column(name = "direccion", length = 120)
    private String direccion;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "cod_establecimiento", length = 3)
    private String codEstablecimiento;

    @Column(name = "cod_punto_emision", length = 3)
    private String codPuntoEmision;
}
