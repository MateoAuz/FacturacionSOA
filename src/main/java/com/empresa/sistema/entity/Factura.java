package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(name = "numero_secuencial", nullable = false, unique = true, length = 17)
    private String numeroSecuencial;

    // ── Datos del cliente / usuario / sucursal ────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iva", nullable = false)
    private ConfiguracionIva configuracionIva;

    // ── Datos económicos ──────────────────────────────────────────
    @Builder.Default
    @Column(name = "fecha_factura", nullable = false)
    private LocalDateTime fechaFactura = LocalDateTime.now();

    @Builder.Default
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "iva_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal ivaValor = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 15)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoFactura estado = EstadoFactura.GUARDADA;

    @Column(name = "observacion", length = 200)
    private String observacion;

    // ── Snapshot en el momento de la venta (inmutable para auditoría) ──
    /** Tipo de identificación del cliente al momento de facturar */
    @Column(name = "snap_cli_tipo_id", length = 10)
    private String snapCliTipoId;

    /** Número de identificación del cliente al momento de facturar */
    @Column(name = "snap_cli_identificacion", length = 20)
    private String snapCliIdentificacion;

    /** Nombres completos del cliente al momento de facturar */
    @Column(name = "snap_cli_nombres", length = 60)
    private String snapCliNombres;

    /** Apellidos del cliente al momento de facturar */
    @Column(name = "snap_cli_apellidos", length = 60)
    private String snapCliApellidos;

    /** Razón social del cliente (si aplica) */
    @Column(name = "snap_cli_razon_social", length = 100)
    private String snapCliRazonSocial;

    /** Correo del cliente al momento de facturar */
    @Column(name = "snap_cli_correo", length = 80)
    private String snapCliCorreo;

    /** Teléfono del cliente al momento de facturar */
    @Column(name = "snap_cli_telefono", length = 15)
    private String snapCliTelefono;

    /** Dirección del cliente al momento de facturar */
    @Column(name = "snap_cli_direccion", length = 120)
    private String snapCliDireccion;

    /** Nombre completo del vendedor al momento de facturar */
    @Column(name = "snap_usuario_nombre", length = 100)
    private String snapUsuarioNombre;

    /** Nombre de la sucursal al momento de facturar */
    @Column(name = "snap_sucursal_nombre", length = 80)
    private String snapSucursalNombre;

    /** Ciudad de la sucursal al momento de facturar */
    @Column(name = "snap_sucursal_ciudad", length = 80)
    private String snapSucursalCiudad;

    /** Porcentaje de IVA aplicado al momento de facturar */
    @Column(name = "snap_iva_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal snapIvaPorcentaje;

    // ── Campos SRI (Fase 2) ───────────────────────────────────────
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "pdf_path", length = 255)
    private String pdfPath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sri", nullable = false, length = 15)
    private EstadoSri estadoSri = EstadoSri.NO_ENVIADO;

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

    @Column(name = "mensaje_sri", length = 500)
    private String mensajeSri;

    // ── Enums ────────────────────────────────────────────────────
    public enum MetodoPago   { EFECTIVO, TARJETA, TRANSFERENCIA }
    public enum EstadoFactura { GUARDADA, EMITIDA, ANULADA }
    public enum EstadoSri    { NO_ENVIADO, ENVIADO, AUTORIZADO, RECHAZADO }
}
