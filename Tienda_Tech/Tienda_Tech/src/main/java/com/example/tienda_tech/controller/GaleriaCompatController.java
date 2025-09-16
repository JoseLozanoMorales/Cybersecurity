// src/main/java/com/example/tienda_tech/controller/GaleriaCompatController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/galeria")
@RequiredArgsConstructor
public class GaleriaCompatController {

  private final GaleriaV2Service v2;

  @DeleteMapping("/{galeriaId}")
  public ResponseEntity<Void> eliminar(@PathVariable int galeriaId) {
    v2.eliminar(galeriaId);
    return ResponseEntity.noContent().build();
  }
}
