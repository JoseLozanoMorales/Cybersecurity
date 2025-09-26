package com.example.tienda_tech.controller;
import com.example.tienda_tech.dto.SugerenciaPcRequest;
import com.example.tienda_tech.dto.SugerenciaPcResponse;
import com.example.tienda_tech.service.SugerenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sugerencias")
public class SugerenciaController {

    private final SugerenciaService service;
    public SugerenciaController(SugerenciaService service) { this.service = service; }

    @PostMapping("/pc-completa")
    public ResponseEntity<SugerenciaPcResponse> generarPcCompleta(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestBody SugerenciaPcRequest body) {

        // Ajusta cómo obtienes el usuario (token, sesión, etc.). Fallback a 'anon'.
        if (username == null || username.isBlank()) username = "anon";
        return ResponseEntity.ok(service.generarPcCompleta(username, body));
    }
}
