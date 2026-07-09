package com.example.tienda_tech.controller;

import com.example.tienda_tech.repository.GaleriaContenidoView;
import com.example.tienda_tech.repository.GaleriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.time.Duration;

@RestController
@RequestMapping("/api/galeria")
@RequiredArgsConstructor
public class GaleriaMenuController {

    private final GaleriaRepository repo;

    @GetMapping("/api/galeria-menu/{id}/contenido")
    public ResponseEntity<?> contenidoMenu(@PathVariable Long id) {
        var opt = repo.findByGaleriaIdAndHabilitadoTrue(id, GaleriaContenidoView.class);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        var v = opt.get();
        var data = v.getContenido();
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();

        MediaType mt;
        try {
            mt = (v.getMimeType() == null || v.getMimeType().isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(v.getMimeType());
        } catch (Exception e) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mt)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(data);
    }
}
