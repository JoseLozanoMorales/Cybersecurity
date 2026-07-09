package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id", nullable = false)
    private Integer movimientoId;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "subtipo_id")
    private Integer subtipoId;

}
