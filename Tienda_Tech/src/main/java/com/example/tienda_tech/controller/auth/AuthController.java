package com.example.tienda_tech.controller.auth;

import com.example.tienda_tech.service.SiemAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** La sesión actual es stateless: cerrar sesión elimina el token en el cliente. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SiemAuditService siem;

    public AuthController(SiemAuditService siem) {
        this.siem = siem;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        siem.registrarEvento("LOGOUT", authentication.getName(), "Autenticación",
                "Exitoso", "El usuario cerró su sesión.", "INFO");
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
