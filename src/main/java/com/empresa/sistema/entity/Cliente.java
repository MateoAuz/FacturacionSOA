package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificacion", nullable = false, length = 10)
    private TipoIdentificacion tipoIdentificacion = TipoIdentificacion.CEDULA;

    @Column(name = "identificacion", nullable = false, length = 20)
    private String identificacion;

    @Column(name = "nombres", nullable = false, length = 60)
    private String nombres;

    @Column(name = "apellidos", length = 60)
    private String apellidos;

    @Column(name = "razon_social", length = 100)
    private String razonSocial;

    @Column(name = "direccion", length = 120)
    private String direccion;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "correo", length = 80)
    private String correo;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum TipoIdentificacion { CEDULA, RUC, PASAPORTE }
}
