package com.example.tienda_tech.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductReportRow {
    private Integer productoId;
    private String  nombre;
    private BigDecimal precioUnitario;
    private Integer stock;
    private BigDecimal costo;
    private LocalDate fecha;
    private Boolean habilitado;
}
