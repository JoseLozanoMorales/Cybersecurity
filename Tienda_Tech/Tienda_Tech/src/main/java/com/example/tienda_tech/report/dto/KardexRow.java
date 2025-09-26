package com.example.tienda_tech.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KardexRow {
    private LocalDate fecha;           // orden.fecha
    private Integer  productoId;       // producto.producto_id
    private String   producto;         // producto.nombre
    private String   movimiento;       // "SALIDA"
    private int      cantidad;         // detalle_orden.cantidad
    private BigDecimal precioUnitario; // detalle_orden.precio_unitario
    private BigDecimal subtotal;       // detalle_orden.subtotal
    private BigDecimal iva;            // detalle_orden.iva
    private BigDecimal total;          // detalle_orden.total
}
