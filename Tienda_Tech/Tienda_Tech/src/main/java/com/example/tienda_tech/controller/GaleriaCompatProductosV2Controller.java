// src/main/java/com/example/tienda_tech/controller/GaleriaCompatProductosV2Controller.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.GaleriaV2Dtos.GaleriaItemDto;
import com.example.tienda_tech.service.GaleriaV2Service;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class GaleriaCompatProductosV2Controller {

  private final GaleriaV2Service v2;

  // GET /api/productos/{id}/galeria/lista?vista=galeria|menu
  @GetMapping("/{productoId}/galeria/lista")
  public List<GaleriaItemDto> listar(@PathVariable int productoId,
                                     @RequestParam(defaultValue="galeria") String vista) {
    return v2.listar(productoId, vista);
  }

  // POST /api/productos/{productoId}/galeria/{galeriaId}/portada
  @PostMapping("/{productoId}/galeria/{galeriaId}/portada")
  public ResponseEntity<Void> portada(@PathVariable int productoId, @PathVariable int galeriaId){
    v2.setPortada(productoId, galeriaId);
    return ResponseEntity.noContent().build();
  }

  // POST /api/productos/{productoId}/galeria/reordenar  (body: { vista, items:[{galeriaId,posicion}] })
  @PostMapping("/{productoId}/galeria/reordenar")
  public ResponseEntity<Void> reordenar(@PathVariable int productoId, @RequestBody ReordenarVistaReq body){
    var scope = "menu".equalsIgnoreCase(body.getVista()) ? "menu" : "galeria";
    var ids = body.getItems().stream()
      .sorted(Comparator.comparingInt(Item::getPosicion))
      .map(Item::getGaleriaId).toList();
    v2.reordenar(productoId, scope, ids);
    return ResponseEntity.noContent().build();
  }

  // POST /api/productos/{productoId}/galeria  (JSON batch con base64)
@PostMapping(value="/{productoId}/galeria", consumes = "application/json")
public ResponseEntity<?> subirJson(@PathVariable int productoId, @RequestBody List<UploadItem> items){
  int ok = 0;
  var errs = new java.util.ArrayList<String>();

  for (int idx = 0; idx < items.size(); idx++){
    var it = items.get(idx);
    try {
      String b64 = it.getBytesB64();
      if (b64 == null || b64.isBlank()) {
        errs.add("#"+idx+": bytes_b64 vacío");
        continue;
      }
      // soporta "data:image/...;base64,AAAA"
      int comma = b64.indexOf(',');
      if (comma > 0 && b64.substring(0, comma).contains(";base64")) {
        b64 = b64.substring(comma + 1);
      }
      byte[] bytes = java.util.Base64.getDecoder().decode(b64);

      // Log útil
      org.slf4j.LoggerFactory.getLogger(getClass())
          .info("[GALv2] prod={}, item#{}, mime={}, b64len={}, bytes={}",
                productoId, idx, it.getMime_type(),
                (it.getBytesB64()==null?0:it.getBytesB64().length()), bytes.length);

      v2.agregarBytes(
        productoId,
        bytes,
        it.getMime_type(),
        it.getDescripcion(),
        Boolean.FALSE,
        it.getPara_galeria() == null ? Boolean.TRUE  : it.getPara_galeria(),
        it.getPara_menu()    == null ? Boolean.FALSE : it.getPara_menu(),
        null, null, null, null
      );
      ok++;
    } catch (Exception e) {
      errs.add("#"+idx+": "+e.getClass().getSimpleName()+" "+e.getMessage());
    }
  }

  var body = java.util.Map.of("uploaded", ok, "errors", errs);
  if (ok == 0) return ResponseEntity.badRequest().body(body);
  return ResponseEntity.ok(body);
}


  /* ==== DTOs para compat ==== */
  @Data public static class Item { private Integer galeriaId; private Integer posicion; }
  @Data public static class ReordenarVistaReq { private String vista; private List<Item> items; }

  @Data
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class UploadItem {
    private String  descripcion;
    private String  mime_type;
    private String  bytesB64;    // mapea "bytes_b64" -> bytesB64
    private Boolean para_galeria;
    private Boolean para_menu;
    private Boolean habilitado;
  }
}
