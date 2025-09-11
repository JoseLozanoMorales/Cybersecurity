package com.example.tienda_tech.dto.galeria;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GaleriaUploadItemDto {

  private String descripcion;

  @JsonProperty("mime_type")
  @JsonAlias({"mimeType"})
  private String mimeType;

  @JsonProperty("contenido_base64")
  @JsonAlias({"contenidoBase64","base64"})
  private String contenidoBase64;

  @JsonProperty("para_menu")
  @JsonAlias({"paraMenu"})
  private Boolean paraMenu;

  private Boolean habilitado;
}
