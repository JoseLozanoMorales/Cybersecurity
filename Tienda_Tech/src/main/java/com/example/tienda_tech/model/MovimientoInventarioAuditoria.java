package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimiento_inventario_auditoria")
public class MovimientoInventarioAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditoria_id")
    private Long auditoriaId;

    @Column(name = "movimiento_id")
    private Integer movimientoId;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "cantidad")
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

    @Column(name = "fechahorareg")
    private LocalDateTime fechaHoraReg;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "tipo", length = 1)
    private String tipo;
}
