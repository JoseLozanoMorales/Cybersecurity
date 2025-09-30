package com.example.tienda_tech.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LowStockRow {
    private Integer productoId; private String nombre;
    private Integer stock; private BigDecimal precioUnitario;
    private LocalDate fecha; private Boolean habilitado;
}
