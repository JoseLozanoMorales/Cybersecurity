package com.example.tienda_tech.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderReportRow {
    private Integer ordenId;
    private LocalDate fecha;
    private Integer usuarioId;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
}
