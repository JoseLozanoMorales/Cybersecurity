package com.example.tienda_tech.dto.audit;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAuditoriaDTO {
    private LocalDateTime fechahorareg;
    private String usuario;
    private String tipo;
    private Integer productoId;

    private String nombre;           // antes
    private String nombreDespues;    // después

    private BigDecimal precioUnitario;
    private BigDecimal precioUnitarioDespues;

    private Integer stock;
    private Integer stockDespues;

    private Integer ivaId;
    private Integer ivaIdDespues;

    private Boolean habilitado;
    private Boolean habilitadoDespues;
}
