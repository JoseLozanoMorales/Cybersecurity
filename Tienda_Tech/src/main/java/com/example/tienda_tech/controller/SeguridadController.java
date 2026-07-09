package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seguridad")
@RequiredArgsConstructor
public class SeguridadController {

    private final UsuarioService usuarioService;

    public record ChangePwdReq(String actual, String nueva) {}
    public record ChangePwdTokenReq(String token, String actual, String nueva) {}

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(HttpServletRequest req, @RequestBody ChangePwdReq body) {
        usuarioService.cambiarPasswordSesion(req, body.actual(), body.nueva());
        return ResponseEntity.ok(Map.of("ok", true, "message", "Contraseña actualizada"));
    }

    @PostMapping("/cambiar-password-token")
    public ResponseEntity<?> cambiarPasswordToken(@RequestBody ChangePwdTokenReq body) {
        usuarioService.cambiarPasswordConToken(body.token(), body.actual(), body.nueva());
        return ResponseEntity.ok(Map.of("ok", true, "message", "Contraseña actualizada"));
    }
}
