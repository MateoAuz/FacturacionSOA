package com.empresa.sistema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "configuracion_iva")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfiguracionIva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_iva")
    private Integer idIva;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "vigencia_desde", nullable = false)
    private LocalDate vigenciaDesde;

    @Column(name = "activo", nullable = false)
    private Boolean activo = false;
}
