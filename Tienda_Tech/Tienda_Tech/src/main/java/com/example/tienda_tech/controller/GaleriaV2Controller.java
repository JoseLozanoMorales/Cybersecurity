// src/main/java/com/example/tienda_tech/controller/GaleriaV2Controller.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.GaleriaV2Dtos.*;
import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

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

    // === ENDURECIDO ===
    @GetMapping("/img/{galeriaId}")
    public ResponseEntity<? extends Resource> obtenerImagen(@PathVariable Integer galeriaId) {
        var media = service.obtenerMedia(galeriaId);
        if (media == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ítem no existe");

        // Si tu record/DTO tiene "habilitado", descomenta:
        // if (Boolean.FALSE.equals(media.habilitado()))
        //   throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ítem deshabilitado");

        // Soporte para dos almacenamientos: bytes en BD o archivo en disco (storagePath)
        byte[] bytes = media.bytes();
        Path   path  = null;
        try {
            // si tu DTO expone storagePath() o similar, descomenta:
            // path = (media.storagePath() != null && !media.storagePath().isBlank())
            //          ? Path.of(media.storagePath()) : null;
        } catch (Exception ignore) {}

        if ((bytes == null || bytes.length == 0) && (path == null || !Files.exists(path)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "sin contenido");

        MediaType mt;
        try {
            mt = (media.mimeType() != null && !media.mimeType().isBlank())
                    ? MediaType.parseMediaType(media.mimeType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        // ETag simple (opcional)
        String etag = null;
        try {
            CRC32 crc = new CRC32();
            if (bytes != null) crc.update(bytes);
            else crc.update(Files.readAllBytes(path));
            etag = "W/\"" + crc.getValue() + "\"";
        } catch (Exception ignore) {}

        ResponseEntity.BodyBuilder resp = ResponseEntity.ok().contentType(mt)
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(5)).cachePublic());
        if (etag != null) resp.eTag(etag);

        if (bytes != null && bytes.length > 0) {
            return resp
                    .contentLength(media.length() != null ? media.length() : bytes.length)
                    .body(new ByteArrayResource(bytes));
        } else {
            try {
                return resp
                        .contentLength(Files.size(path))
                        .body(new FileSystemResource(path));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "archivo no disponible");
            }
        }
    }

    // HEAD liviano (200/404/403) sin cuerpo
    @RequestMapping(path = "/img/{galeriaId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headImagen(@PathVariable Integer galeriaId) {
        var media = service.obtenerMedia(galeriaId);
        if (media == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // if (Boolean.FALSE.equals(media.habilitado())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if ((media.bytes() == null || media.bytes().length == 0)) {
            // si usas storagePath, podrías verificar existencia aquí
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }

    // ==== Resto de endpoints (igual que ya tienes) ====
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> agregar(
            @RequestParam Integer productoId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("uploaded", 1, "ids", List.of(id), "errors", List.of()));
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
