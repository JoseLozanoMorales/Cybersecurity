package com.example.tienda_tech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProductoRecienteMenuDto {
    private Integer productoId;
    private String  nombre;
    private BigDecimal precio;
    private LocalDate fecha;
    private Long    galeriaId;
    private String  mimeType;
}
