package com.example.tienda_tech.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaFlagsItem {
  private Integer galeria_id;
  private Boolean para_galeria;  // null = sin cambio
  private Boolean para_menu;     // null = sin cambio
  private Boolean habilitado;    // null = sin cambio
  private String  descripcion;   // null = sin cambio
}
