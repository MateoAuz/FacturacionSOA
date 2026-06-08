package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_empresa")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfiguracionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Integer idConfiguracion;

    @Column(name = "razon_social", nullable = false, length = 100)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 100)
    private String nombreComercial;

    @Column(name = "ruc", nullable = false, length = 13)
    private String ruc;

    @Column(name = "direccion_matriz", length = 200)
    private String direccionMatriz;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "correo", length = 80)
    private String correo;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    @Column(name = "obligado_contabilidad", length = 2)
    private String obligadoContabilidad;

    @Column(name = "contribuyente_especial", length = 15)
    private String contribuyenteEspecial;

    @Column(name = "ambiente")
    private Integer ambiente;

    @Column(name = "ruta_certificado", length = 255)
    private String rutaCertificado;

    @Column(name = "clave_certificado", length = 100)
    private String claveCertificado;
}
