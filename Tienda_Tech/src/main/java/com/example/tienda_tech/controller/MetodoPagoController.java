package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.MetodoPagoDTO;
import com.example.tienda_tech.service.MetodoPagoService;
import com.example.tienda_tech.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final MetodoPagoService service;

    private Integer resolveUserId(HttpServletRequest req){
        return AuthenticatedUser.id();
    }

    // Catálogo para el <select>
    @GetMapping("/tipo_metodopago")
    public List<MetodoPagoDTO> tipos() {
        return service.listarTipos();
    }

    // Lista "mis métodos"
    @GetMapping("/mis-metodos-pago")
    public List<MetodoPagoDTO> listar(HttpServletRequest req) {
        return service.listar(resolveUserId(req));
    }

    // Crear
    @PostMapping("/mis-metodos-pago")
    public ResponseEntity<List<MetodoPagoDTO>> crear(@RequestBody @Valid MetodoPagoDTO body,
                                                     HttpServletRequest req) {
        Integer userId = resolveUserId(req);
        service.crear(userId, body);
        var lista = service.listar(userId);
        return ResponseEntity.created(URI.create("/api/mis-metodos-pago")).body(lista);
    }

    // Eliminar
    @DeleteMapping("/mis-metodos-pago/{metodoId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer metodoId, HttpServletRequest req) {
        service.eliminar(resolveUserId(req), metodoId);
        return ResponseEntity.noContent().build();
    }
}
