package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer idInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();
}
