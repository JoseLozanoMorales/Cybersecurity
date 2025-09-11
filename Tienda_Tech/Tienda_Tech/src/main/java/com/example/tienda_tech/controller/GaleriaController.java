// src/main/java/com/example/tienda_tech/controller/GaleriaController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.galeria.*;
import com.example.tienda_tech.model.Galeria;
import com.example.tienda_tech.service.GaleriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class GaleriaController {
  private final GaleriaService service;

  // lista
  @GetMapping("/api/productos/{productoId}/galeria/lista")
  public List<GaleriaItemDto> listar(@PathVariable long productoId) {
    return service.listar(productoId);
  }

  // subir batch (JSON)
  @PostMapping(value="/api/productos/{productoId}/galeria", consumes=MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> subir(
      @PathVariable Long productoId,
      @RequestBody List<GaleriaUploadItemDto> items) {
    List<Integer> ids = service.subir(productoId, items);   // <-- era List<Long>
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("creados", ids));
  }

  // flags/ALT
  @PatchMapping("/api/galeria/flags")
  public Map<String, Object> flags(@RequestBody List<GaleriaFlagDto> flags) {
    service.guardarFlags(flags);
    return Map.of("ok", true);
  }

  // reordenar en vista
  @PostMapping("/api/productos/{productoId}/galeria/reordenar")
  public Map<String, Object> reordenar(
      @PathVariable Long productoId,
      @RequestBody GaleriaReorderDto dto) {
    service.reordenar(productoId, dto);
    return Map.of("ok", true);
  }

  // portada
  @PostMapping("/api/productos/{productoId}/galeria/{galeriaId}/portada")
  public Map<String, Object> portada(
      @PathVariable Long productoId,
      @PathVariable Integer galeriaId) {                    // <-- era Long
    service.marcarPortada(productoId, galeriaId);
    return Map.of("ok", true);
  }

  // media binaria
  @GetMapping("/api/galeria/{galeriaId}/media")
  public ResponseEntity<byte[]> media(@PathVariable Integer galeriaId) { // <-- era Long
  Optional<Galeria> opt = service.obtener(galeriaId);
    if (opt.isEmpty() || opt.get().getContenido()==null)
      return ResponseEntity.notFound().build();

    Galeria g = opt.get();
    MediaType mt;
    try { mt = MediaType.parseMediaType(g.getMimeType()); }
    catch (Exception e) { mt = MediaType.APPLICATION_OCTET_STREAM; }

    return ResponseEntity.ok()
      .contentType(mt)
      .contentLength(g.getPesoBytes() != null ? g.getPesoBytes() : g.getContenido().length)
      .body(g.getContenido());
  }

  // eliminar (soft por defecto; ?hard=true borra)
  @DeleteMapping("/api/galeria/{galeriaId}")
  public Map<String, Object> eliminar(
      @PathVariable Integer galeriaId,                      // <-- era Long
      @RequestParam(name="hard", defaultValue = "false") boolean hard) {
    service.eliminar(galeriaId, hard);
    return Map.of("ok", true);
  }
}
