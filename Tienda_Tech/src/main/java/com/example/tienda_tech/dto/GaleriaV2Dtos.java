// src/main/java/com/example/tienda_tech/dto/GaleriaV2Dtos.java
package com.example.tienda_tech.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class GaleriaV2Dtos {

  @Data
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class GaleriaItemDto {
    private Integer galeriaId;
    private String  descripcion;
    private Boolean esPortada;
    private Boolean paraGaleria;
    private Boolean paraMenu;
    private Integer posicionGaleria;
    private Integer posicionMenu;
    private String  mimeType;
    private Long    pesoBytes;
    private Integer ancho;
    private Integer alto;
    private Boolean habilitado;
  }

  @Data
  public static class ReordenarReq { private List<Integer> ids; }

  @Data
  public static class PortadaReq { private Integer productoId; }

  @Data
  public static class FlagsDto {
    private Boolean habilitado;
    private Boolean paraGaleria;
    private Boolean paraMenu;
  }
}
