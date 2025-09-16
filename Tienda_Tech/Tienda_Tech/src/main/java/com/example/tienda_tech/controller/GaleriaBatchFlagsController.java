// src/main/java/com/example/tienda_tech/controller/GaleriaBatchFlagsController.java
package com.example.tienda_tech.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/galeria")
@RequiredArgsConstructor
public class GaleriaBatchFlagsController {

  private final GaleriaV2Service v2;

  @PatchMapping("/flags")
  public ResponseEntity<Void> flags(@RequestBody List<FlagsPayload> body){
    for (var it: body){
      v2.actualizarFlagsYDescripcion(
          it.getGaleriaId(), it.getHabilitado(), it.getParaGaleria(), it.getParaMenu(), it.getDescripcion()
      );
    }
    return ResponseEntity.noContent().build();
  }

  @Data
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class FlagsPayload {
    private Integer galeriaId;
    private Boolean paraGaleria;
    private Boolean paraMenu;
    private Boolean habilitado;
    private String  descripcion;
  }
}
