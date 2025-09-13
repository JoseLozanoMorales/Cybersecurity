// src/main/java/com/example/tienda_tech/controller/GaleriaMediaController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.GaleriaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")   // <-- sin /productos
@RequiredArgsConstructor
public class GaleriaMediaController {

  private final GaleriaQueryService svc;

  @GetMapping("/galeria/{galeriaId}/media")
  public ResponseEntity<byte[]> media(@PathVariable int galeriaId){
    var m = svc.obtenerMedia(galeriaId);
    if (m == null) return ResponseEntity.notFound().build();
    var mt = (m.mime()!=null) ? MediaType.parseMediaType(m.mime())
                              : MediaType.APPLICATION_OCTET_STREAM;
    return ResponseEntity.ok().contentType(mt).body(m.bytes());
  }
}
