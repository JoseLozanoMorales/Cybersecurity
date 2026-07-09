package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orden_id", nullable = false)
    private Integer ordenId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "direccion_id", nullable = false)
    private Integer direccionId;

    @Column(name = "metodopago_id", nullable = false)
    private Integer metodoPagoId;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha")
    private LocalDate fecha;
}
