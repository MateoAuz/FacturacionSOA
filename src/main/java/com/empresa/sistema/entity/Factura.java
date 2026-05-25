package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @Column(name = "numero_secuencial", nullable = false, unique = true, length = 17)
    private String numeroSecuencial;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "pdf_path", length = 255)
    private String pdfPath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoFactura estado = EstadoFactura.EMITIDA;

    @Column(name = "clave_acceso", length = 49)
    private String claveAcceso;

    @Column(name = "xml_generado", columnDefinition = "LONGTEXT")
    private String xmlGenerado;

    @Column(name = "xml_firmado", columnDefinition = "LONGTEXT")
    private String xmlFirmado;

    @Column(name = "xml_autorizado", columnDefinition = "LONGTEXT")
    private String xmlAutorizado;

    @Column(name = "numero_autorizacion", length = 49)
    private String numeroAutorizacion;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sri", nullable = false, length = 15)
    private EstadoSri estadoSri = EstadoSri.NO_ENVIADO;

    @Column(name = "mensaje_sri", length = 500)
    private String mensajeSri;

    public enum EstadoFactura { EMITIDA, ANULADA }
    public enum EstadoSri { NO_ENVIADO, ENVIADO, AUTORIZADO, RECHAZADO }
}