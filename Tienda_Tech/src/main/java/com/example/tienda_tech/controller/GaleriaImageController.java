// src/main/java/com/example/tienda_tech/controller/GaleriaImageController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/galeria")
@RequiredArgsConstructor
public class GaleriaImageController {

  private final GaleriaV2Service v2;

  @GetMapping("/img/{galeriaId}")
  public ResponseEntity<ByteArrayResource> img(@PathVariable int galeriaId){
    var m = v2.obtenerMedia(galeriaId);
    if (m == null || m.bytes() == null) return ResponseEntity.notFound().build();
    MediaType mt;
    try { mt = MediaType.parseMediaType(m.mimeType()==null ? "application/octet-stream" : m.mimeType()); }
    catch (Exception e) { mt = MediaType.APPLICATION_OCTET_STREAM; }
    return ResponseEntity.ok()
        .contentType(mt)
        .contentLength(m.length()==null? m.bytes().length : m.length())
        .cacheControl(CacheControl.noCache())
        .body(new ByteArrayResource(m.bytes()));
  }
}
