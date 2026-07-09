package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class RecuperarPasswordController {

    private final UsuarioService usuarioService;

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperar(@RequestBody Map<String, String> body) {
        String correo = (body.getOrDefault("correo", "")).trim();
        if (correo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El correo es requerido"));
        }

        usuarioService.resetearPasswordYNotificarPorCorreo(correo);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Se envió la contraseña temporal"));
    }
}
