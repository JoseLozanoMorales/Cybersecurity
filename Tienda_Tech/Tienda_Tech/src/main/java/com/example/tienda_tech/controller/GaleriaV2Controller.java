// src/main/java/com/example/tienda_tech/controller/GaleriaV2Controller.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.GaleriaV2Dtos.*;
import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/galeria_v2")
@RequiredArgsConstructor
public class GaleriaV2Controller {

    private final GaleriaV2Service service;

    @GetMapping("/producto/{productoId}")
    public List<GaleriaItemDto> listar(@PathVariable Integer productoId,
                                       @RequestParam(required = false) String scope) {
        return service.listar(productoId, scope);
    }

    @GetMapping("/img/{galeriaId}")
    public ResponseEntity<ByteArrayResource> obtenerImagen(@PathVariable Integer galeriaId) {
        var media = service.obtenerMedia(galeriaId);
        if (media == null || media.bytes() == null) return ResponseEntity.notFound().build();
        ByteArrayResource body = new ByteArrayResource(media.bytes());
        MediaType mt;
        try { mt = MediaType.parseMediaType(media.mimeType() == null ? "application/octet-stream" : media.mimeType()); }
        catch (Exception e) { mt = MediaType.APPLICATION_OCTET_STREAM; }

        return ResponseEntity.ok()
                .contentType(mt)
                .contentLength(media.length() == null ? media.bytes().length : media.length())
                .cacheControl(CacheControl.noCache())
                .body(body);
    }

    // ✅ ahora devuelve { uploaded, ids, errors } y usa la FUNCIÓN que retorna galeria_id
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> agregar(
            @RequestParam Integer productoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false, defaultValue = "false") Boolean esPortada,
            @RequestParam(required = false, defaultValue = "true")  Boolean paraGaleria,
            @RequestParam(required = false, defaultValue = "false") Boolean paraMenu,
            @RequestParam(required = false) Integer posGaleria,
            @RequestParam(required = false) Integer posMenu,
            @RequestParam(required = false) Integer ancho,
            @RequestParam(required = false) Integer alto
    ) {
        int id = service.agregar(productoId, file, descripcion, esPortada, paraGaleria, paraMenu,
                posGaleria, posMenu, ancho, alto);
        Map<String,Object> body = Map.of(
                "uploaded", 1,
                "ids", List.of(id),
                "errors", List.of()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{galeriaId}/portada")
    public ResponseEntity<Void> portada(@PathVariable Integer galeriaId, @RequestBody PortadaReq req) {
        service.setPortada(req.getProductoId(), galeriaId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productoId}/reordenar")
    public ResponseEntity<Void> reordenar(@PathVariable Integer productoId,
                                          @RequestParam String scope,
                                          @RequestBody ReordenarReq body) {
        service.reordenar(productoId, scope, body.getIds());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{galeriaId}/flags")
    public ResponseEntity<Void> flags(@PathVariable Integer galeriaId, @RequestBody FlagsDto f) {
        service.actualizarFlags(galeriaId, f.getHabilitado(), f.getParaGaleria(), f.getParaMenu());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{galeriaId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer galeriaId) {
        service.eliminar(galeriaId);
        return ResponseEntity.noContent().build();
    }
}
