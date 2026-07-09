package com.example.tienda_tech.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaReordenarRequest {
  // "galeria" | "menu"
  private String vista;
  private List<Item> items;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Item {
    private Integer galeriaId;
    private Integer posicion;
  }
}
