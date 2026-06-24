// src/main/java/com/example/tienda_tech/controller/LoginController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.service.UsuarioService;
import com.example.tienda_tech.service.SiemAuditService;
import com.example.tienda_tech.service.audit.UsuarioAuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioAuditoriaService usuarioAuditoriaService;

    @Autowired
    private SiemAuditService siemAuditService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario");
        String contrasenia = body.get("contrasena");

        // Validación de campos vacíos (del anterior)
        if (usuario == null || usuario.trim().isEmpty() || contrasenia == null || contrasenia.trim().isEmpty()) {
            siemAuditService.registrarEvento("CAMPOS_VACIOS", usuario, "Autenticación", "Denegado",
                    "Intento de inicio de sesión con campos vacíos.", "ADVERTENCIA");
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Debe ingresar usuario y contraseña."));
        }

        try {
            var u = usuarioService.login(usuario, contrasenia);

            // Auditoría de roles (del actual)
            int rol = (u.getIdRol() == null) ? 0 : u.getIdRol();
            if (rol == 1 || rol == 3) {
                usuarioAuditoriaService.registrarLogin(u.getUsuarioId());
            }

            Map<String, Object> userPayload = Map.of(
                    "usuarioId", u.getUsuarioId(),
                    "usuario",   u.getUsuario(),
                    "nombre",    u.getNombre(),
                    "cedula",    u.getCedula(),
                    "correo",    u.getCorreo(),
                    "telefono",  u.getTelefono(),
                    "id_rol",    u.getIdRol()
            );

            // Evento SIEM exitoso (del anterior)
            siemAuditService.registrarEvento("LOGIN_EXITOSO", usuario, "Autenticación", "Permitido",
                    "El usuario inició sesión correctamente en TiendaTech.", "INFO");

            return ResponseEntity.ok(Map.of("success", true, "user", userPayload, "token", "mock"));

        } catch (Exception e) {
            // Evento SIEM fallido (del anterior)
            siemAuditService.registrarEvento("LOGIN_FALLIDO", usuario, "Autenticación", "Denegado",
                    "Credenciales incorrectas: " + e.getMessage(), "ALERTA");
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Usuario o contraseña incorrectos."));
        }
    }
}