package com.example.tienda_tech.dto.galeria;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaReorderDto {

  /** "galeria" | "menu" */
  private String vista;

  private List<Item> items;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Item {
    @JsonProperty("galeria_id")
    @JsonAlias({"galeriaId"})
    private Long galeriaId;

    private int posicion;
  }
}
