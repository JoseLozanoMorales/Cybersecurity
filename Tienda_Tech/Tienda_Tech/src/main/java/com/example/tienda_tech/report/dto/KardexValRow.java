package com.example.tienda_tech.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del Kardex Valorizado.
 * Los nombres de los campos están pensados para que existan setters
 * exactamente: setMov, setCantidad, setPrecio, setSaldoCant, setSaldoTotal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KardexValRow {

    private LocalDate fecha;
    private Integer   productoId;
    private String    producto;

    /** "ENTRADA" o "SALIDA" */
    private String    mov;

    /** Cantidad del movimiento (positiva para ENTRADA, positiva para SALIDA) */
    private Integer   cantidad;

    /** Precio/costo unitario usado en el movimiento */
    private BigDecimal precio;

    /** Saldo de unidades después del movimiento */
    private Integer   saldoCant;

    /** Valor total del saldo después del movimiento (ej. costo promedio * saldoCant) */
    private BigDecimal saldoTotal;
}
