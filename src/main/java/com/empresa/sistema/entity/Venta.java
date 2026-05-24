package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "numero_venta", nullable = false, unique = true, length = 15)
    private String numeroVenta;

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

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "iva_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal ivaValor = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 15)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoVenta estado = EstadoVenta.PAGADA;

    @Column(name = "observacion", length = 200)
    private String observacion;

    public enum MetodoPago { EFECTIVO, TARJETA, TRANSFERENCIA }
    public enum EstadoVenta { PAGADA, ANULADA }
}
