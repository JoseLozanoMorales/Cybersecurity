package com.example.tienda_tech.dto.audit;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoAuditoriaDTO {
    private LocalDateTime fecha;          // fecha del movimiento
    private LocalDateTime fechahorareg;   // timestamp de registro
    private String usuario;
    private String tipo;
    private Integer productoId;
    private Integer subtipoId;

    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private String referencia;
    private String observacion;
}
