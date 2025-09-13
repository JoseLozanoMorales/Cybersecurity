// src/main/java/com/example/tienda_tech/controller/GaleriaController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.GaleriaQueryService;
import com.example.tienda_tech.service.GaleriaSpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")   // <--- prefijo de la clase
@RequiredArgsConstructor
public class GaleriaController {

  private final GaleriaSpService galeria;
  private final GaleriaQueryService svc;

  // POST multipart: /api/productos/{id}/galeria
  @PostMapping(value="/{productoId}/galeria",
               consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> subirGaleriaMultipart(
      @PathVariable long productoId,
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam(defaultValue="true")  boolean paraGaleria,
      @RequestParam(defaultValue="false") boolean paraMenu,
      @RequestParam(defaultValue="false") boolean esPortada,
      @RequestParam(required=false) String descripcion
  ){
    int n = galeria.subirDesdeMultipart(productoId, files, paraGaleria, paraMenu, esPortada, descripcion);
    return ResponseEntity.ok(Map.of("uploaded", n));
  }

  // POST JSON: /api/productos/{id}/galeria
  @PostMapping(value="/{productoId}/galeria",
               consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> subirGaleriaJson(
      @PathVariable long productoId,
      @RequestBody List<Map<String,Object>> items
  ){
    int n = galeria.subirDesdeJson(productoId, items);
    return ResponseEntity.ok(Map.of("uploaded", n));
  }

  // GET lista: /api/productos/{id}/galeria/lista
  @GetMapping("/{productoId}/galeria/lista")
  public ResponseEntity<List<GaleriaQueryService.GaleriaItemDto>> listar(
      @PathVariable long productoId,
      @RequestParam(defaultValue="galeria") String vista
  ){
    return ResponseEntity.ok(svc.listar(productoId, vista));
  }

  // GET media: OJO con la ruta resultante
  // Queda /api/productos/galeria/{galeriaId}/media
  @GetMapping("/galeria/{galeriaId}/media")
  public ResponseEntity<byte[]> media(@PathVariable int galeriaId){
    var m = svc.obtenerMedia(galeriaId);
    if (m == null) return ResponseEntity.notFound().build();
    var mt = (m.mime()!=null) ? MediaType.parseMediaType(m.mime())
                              : MediaType.APPLICATION_OCTET_STREAM;
    return ResponseEntity.ok().contentType(mt).body(m.bytes());
  }
}

