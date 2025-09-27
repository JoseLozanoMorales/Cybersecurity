package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "iva")
public class Iva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iva_id", nullable = false)
    private Integer ivaId;

    @Column(name = "porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentaje;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
