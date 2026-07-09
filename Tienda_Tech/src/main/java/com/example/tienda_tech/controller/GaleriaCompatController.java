// src/main/java/com/example/tienda_tech/controller/GaleriaCompatController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.GaleriaV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class GaleriaCompatController {

    private final GaleriaV2Service service;

    @GetMapping("/api/galeria/{galeriaId}/contenido")
    public ResponseEntity<ByteArrayResource> contenidoLegacy(@PathVariable Integer galeriaId) {
        var media = service.obtenerMedia(galeriaId);
        if (media == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ítem no existe");

        // Si tu DTO expone habilitado(), descomenta:
        // if (Boolean.FALSE.equals(media.habilitado()))
        //   throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ítem deshabilitado");

        byte[] bytes = media.bytes();
        if (bytes == null || bytes.length == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "sin contenido");

        MediaType mt;
        try {
            mt = (media.mimeType()!=null && !media.mimeType().isBlank())
                    ? MediaType.parseMediaType(media.mimeType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mt)
                .contentLength(media.length()!=null ? media.length() : bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    // Pre-chequeo sin cuerpo
    @RequestMapping(path="/api/galeria/{galeriaId}/contenido", method=RequestMethod.HEAD)
    public ResponseEntity<Void> headLegacy(@PathVariable Integer galeriaId) {
        var m = service.obtenerMedia(galeriaId);
        if (m == null || m.bytes()==null || m.bytes().length==0) return ResponseEntity.status(404).build();
        // if (Boolean.FALSE.equals(m.habilitado())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok().build();
    }
}
