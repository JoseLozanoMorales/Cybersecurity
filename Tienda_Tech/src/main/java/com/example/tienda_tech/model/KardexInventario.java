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
@Table(name = "kardex_inventario")
public class KardexInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kardex_id", nullable = false)
    private Integer kardexId;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "tipo_operacion", length = 20, nullable = false)
    private String tipoOperacion;

    @Column(name = "cantidad_entrada")
    private Integer cantidadEntrada; // DEFAULT 0

    @Column(name = "costo_unitario_entrada", precision = 10, scale = 2)
    private BigDecimal costoUnitarioEntrada;

    @Column(name = "costo_total_entrada", precision = 12, scale = 2)
    private BigDecimal costoTotalEntrada;

    @Column(name = "cantidad_salida")
    private Integer cantidadSalida; // DEFAULT 0

    @Column(name = "costo_unitario_salida", precision = 10, scale = 2)
    private BigDecimal costoUnitarioSalida;

    @Column(name = "costo_total_salida", precision = 12, scale = 2)
    private BigDecimal costoTotalSalida;

    @Column(name = "saldo_cantidad", nullable = false)
    private Integer saldoCantidad;

    @Column(name = "saldo_costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal saldoCostoUnitario;

    @Column(name = "saldo_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal saldoTotal;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;
}
