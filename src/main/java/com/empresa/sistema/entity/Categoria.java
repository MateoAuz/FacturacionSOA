package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre", nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
