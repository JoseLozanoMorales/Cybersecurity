package com.example.tienda_tech.dto.galeria;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaFlagDto {

  @JsonProperty("galeria_id")
  @JsonAlias({"galeriaId"})
  private Long galeriaId;

  @JsonProperty("para_galeria")
  @JsonAlias({"paraGaleria"})
  private Boolean paraGaleria;

  @JsonProperty("para_menu")
  @JsonAlias({"paraMenu"})
  private Boolean paraMenu;

  private Boolean habilitado;
  private String  descripcion;
}
