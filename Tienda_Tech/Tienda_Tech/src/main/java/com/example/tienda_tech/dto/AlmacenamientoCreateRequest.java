package com.example.tienda_tech.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AlmacenamientoCreateRequest {

    @NotBlank private String nombre;
    @NotNull  private BigDecimal preciounitario;
    private String enlace;                 // opcional
    @NotNull  private Integer stock;
    @NotNull  private Integer marca_id;
    @NotNull  private Integer gama_id;
    @NotNull  private Integer iva_id;
    @NotNull  private BigDecimal costo;

    @NotNull  private Long capacidad;      // GB (en el SP es BIGINT)
    @NotBlank private String tipo;         // "SSD SATA", "SSD NVMe", etc.
}
