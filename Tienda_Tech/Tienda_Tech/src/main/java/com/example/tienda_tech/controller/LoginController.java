// src/main/java/com/example/tienda_tech/controller/LoginController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.security.JwtUtil;
import com.example.tienda_tech.service.OtpService;
import com.example.tienda_tech.service.UsuarioService;
import com.example.tienda_tech.service.audit.UsuarioAuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private OtpService otpService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario");
        String contrasenia = body.get("contrasena");
        // 1. Validar primer factor (Contraseña)
        var u = usuarioService.login(usuario, contrasenia);
        // 2. Generar y enviar OTP por correo usando el servicio existente
        // Reusamos OtpService.enviar() que maneja cooldown y reintentos
        var otpResult = otpService.enviar(u.getCorreo(), null);
        // 3. Responder que se requiere MFA
        return ResponseEntity.ok(Map.of(
                "mfaRequired", true,
                "correo", u.getCorreo(),
                "txId", otpResult.get("txId"),
                "usuarioId", u.getUsuarioId()
        ));
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<?> verificarMfa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String codigo = body.get("codigo");
        String txId = body.get("txId");
        String usuarioIdStr = body.get("usuarioId");

        // 1. Validar el OTP contra la caché
        boolean valid = otpService.validar(correo, codigo, txId);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código de verificación incorrecto o expirado");
        }

        // 2. OTP correcto: Obtener usuario y emitir token final
        Usuario u = usuarioService.getById(Integer.parseInt(usuarioIdStr));

        // Registrar auditoría de login
        int rol = (u.getIdRol() == null) ? 0 : u.getIdRol();
        if (rol == 1 || rol == 3) {
            usuarioAuditoriaService.registrarLogin(u.getUsuarioId());
        }

        // Generar token JWT real usando JwtUtil
        String token = jwtUtil.generateAccess(u.getUsuarioId(), u.getUsuario(), String.valueOf(rol), 60);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                        "usuarioId", u.getUsuarioId(),
                        "usuario", u.getUsuario(),
                        "nombre", u.getNombre(),
                        "correo", u.getCorreo(),
                        "id_rol", u.getIdRol()
                )
        ));
    }
}
