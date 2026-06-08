package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Renombrado de detalle_venta → detalle_factura */
@Entity
@Table(name = "detalle_factura")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal_linea", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalLinea;

    /** Nombre del producto al momento de facturar (snapshot para auditoría) */
    @Column(name = "snap_producto_nombre", length = 100)
    private String snapProductoNombre;

    /** Código del producto al momento de facturar (snapshot para auditoría) */
    @Column(name = "snap_producto_codigo", length = 30)
    private String snapProductoCodigo;
}
