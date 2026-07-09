package com.example.tienda_tech.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KardexValRow {
    // Base
    private LocalDateTime fecha;
    private String detalle;

    // Entrada
    private BigDecimal entCant;   // cantidad
    private BigDecimal entPUnit;  // precio unitario
    private BigDecimal entTotal;  // entCant * entPUnit

    // Salida (PEPS)
    private BigDecimal salCant;   // cantidad
    private BigDecimal salPUnit;  // precio unitario PEPS aplicado
    private BigDecimal salTotal;  // salCant * salPUnit

    // Saldo
    private BigDecimal sldCant;   // cantidad restante
    private BigDecimal sldPUnit;  // costo unitario mostrado
    private BigDecimal sldTotal;  // sldCant * sldPUnit
}
