package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_stock")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SolicitudStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    /** Sucursal del CAJERO que necesita el stock */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_solicitante", nullable = false)
    private Sucursal sucursalSolicitante;

    /** Sucursal que tiene el stock y debe transferirlo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_proveedora", nullable = false)
    private Sucursal sucursalProveedora;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private Estado estado = Estado.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicitante", nullable = false)
    private Usuario usuarioSolicitante;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_respuesta")
    private Usuario usuarioRespuesta;

    public enum Estado { PENDIENTE, ACEPTADA, RECHAZADA }
}
