package com.example.tienda_tech.report.dto;

import lombok.*;

import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesByProductRow {
    private Integer productoId; private String producto;
    private Integer unidades;   private BigDecimal subtotal;
    private BigDecimal iva;     private BigDecimal total;
}
