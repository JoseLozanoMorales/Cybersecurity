// src/main/java/com/example/tienda_tech/controller/LoginController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.security.JwtUtil;
import com.example.tienda_tech.service.OtpService;
import com.example.tienda_tech.service.UsuarioService;
import com.example.tienda_tech.service.SiemAuditService;
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

    @Autowired
    private SiemAuditService siemAuditService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario"); // Changed from "correo" to "usuario"
        String contrasenia = body.get("contrasena");

        if (usuario == null || usuario.trim().isEmpty() || // Changed from "correo" to "usuario"
                contrasenia == null || contrasenia.trim().isEmpty()) {

            siemAuditService.registrarEvento(
                    "CAMPOS_VACIOS",
                    usuario, // Changed from "correo" to "usuario"
                    "Autenticación",
                    "Denegado",
                    "Intento de inicio de sesión con usuario o contraseña vacíos.", // Updated message
                    "ADVERTENCIA"
            );

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Debe ingresar usuario y contraseña." // Updated message
            ));
        }

        try {
            /*
             * Primer factor:
             * Validar usuario y contraseña.
             *
             * IMPORTANTE:
             * Este método debe existir en UsuarioService.
             */
            Usuario u = usuarioService.login(usuario, contrasenia); // Changed to login by username

            /*
             * Segundo factor:
             * Generar y enviar OTP al correo del usuario.
             */
            var otpResult = otpService.enviar(u.getCorreo(), null);

            siemAuditService.registrarEvento(
                    "LOGIN_PRIMER_FACTOR_EXITOSO",
                    usuario, // Changed from "correo" to "usuario"
                    "Autenticación",
                    "Permitido",
                    "Credenciales correctas. Se envió código OTP al correo.",
                    "INFO"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mfaRequired", true,
                    "correo", u.getCorreo(),
                    "txId", otpResult.get("txId"),
                    "usuarioId", u.getUsuarioId()
            ));

        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "LOGIN_FALLIDO",
                    usuario, // Changed from "correo" to "usuario"
                    "Autenticación",
                    "Denegado",
                    "Credenciales incorrectas: " + e.getMessage(),
                    "ALERTA"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "Usuario o contraseña incorrectos." // Updated message
            ));
        }
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<?> verificarMfa(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String codigo = body.get("codigo");
        String txId = body.get("txId");
        String usuarioIdStr = body.get("usuarioId");

        if (correo == null || correo.trim().isEmpty() ||
                codigo == null || codigo.trim().isEmpty() ||
                txId == null || txId.trim().isEmpty() ||
                usuarioIdStr == null || usuarioIdStr.trim().isEmpty()) {

            siemAuditService.registrarEvento(
                    "MFA_CAMPOS_VACIOS",
                    correo,
                    "Autenticación MFA",
                    "Denegado",
                    "Intento de validación MFA con datos incompletos.",
                    "ADVERTENCIA"
            );

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Debe ingresar correo, código, txId y usuarioId."
            ));
        }

        try {
            boolean valid = otpService.validar(correo, codigo, txId);

            if (!valid) {
                siemAuditService.registrarEvento(
                        "MFA_FALLIDO",
                        correo,
                        "Autenticación MFA",
                        "Denegado",
                        "Código de verificación incorrecto o expirado.",
                        "ALERTA"
                );

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "error", "Código de verificación incorrecto o expirado."
                ));
            }

            Usuario u = usuarioService.getById(Integer.parseInt(usuarioIdStr));

            int rol = (u.getIdRol() == null) ? 0 : u.getIdRol();

            if (rol == 1 || rol == 3) {
                usuarioAuditoriaService.registrarLogin(u.getUsuarioId());
            }

            String token = jwtUtil.generateAccess(
                    u.getUsuarioId(),
                    u.getUsuario(),
                    String.valueOf(rol),
                    60
            );

            Map<String, Object> userPayload = Map.of(
                    "usuarioId", u.getUsuarioId(),
                    "usuario", u.getUsuario(),
                    "nombre", u.getNombre(),
                    "cedula", u.getCedula(),
                    "correo", u.getCorreo(),
                    "telefono", u.getTelefono(),
                    "id_rol", u.getIdRol()
            );

            siemAuditService.registrarEvento(
                    "LOGIN_EXITOSO",
                    correo,
                    "Autenticación MFA",
                    "Permitido",
                    "El usuario completó correctamente el inicio de sesión con MFA en TiendaTech.",
                    "INFO"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", token,
                    "user", userPayload
            ));

        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "MFA_ERROR",
                    correo,
                    "Autenticación MFA",
                    "Denegado",
                    "Error durante la validación MFA: " + e.getMessage(),
                    "ALERTA"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "No se pudo validar el código de verificación."
            ));
        }
    }
}