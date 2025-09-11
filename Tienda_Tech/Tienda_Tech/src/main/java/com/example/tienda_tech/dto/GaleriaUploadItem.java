package com.example.tienda_tech.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaUploadItem {
  // producto_id lo inyectamos desde la ruta; no hace falta en el body
  private String descripcion;
  @NotBlank private String mime_type;          // image/jpeg | image/png | image/webp
  @NotBlank private String contenido_base64;   // puede venir con "data:...;base64,"
  private Boolean para_galeria = true;
  private Boolean para_menu = false;
  private Boolean habilitado = true;
  private Integer ancho;                       // opcional
  private Integer alto;                        // opcional
}
