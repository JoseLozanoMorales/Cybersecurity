// src/main/java/com/example/tienda_tech/controller/UsuarioController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.UsuarioMinDTO;
import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.service.SiemAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import com.example.tienda_tech.dto.UsuarioDTO;
import com.example.tienda_tech.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import com.example.tienda_tech.dto.ClienteUpdateRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SiemAuditService siemAuditService;

    /**
     * Registro público (self-service) de clientes.
     * Invoca SP: crear_cliente(...)
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearCliente(@RequestBody UsuarioDTO dto) {
        try {
            usuarioService.crearClienteConSP(dto);

            siemAuditService.registrarEvento(
                    "REGISTRO_CLIENTE",
                    dto.getUsuario() != null ? dto.getUsuario() : "Nuevo",
                    "Usuarios",
                    "Exitoso",
                    "Se registró un nuevo cliente: usuario=\"" + dto.getUsuario() + "\" correo=\"" + dto.getCorreo() + "\".",
                    "INFO"
            );

            return ResponseEntity.ok(Map.of("success", true, "message", "Cliente creado"));
        } catch (DataIntegrityViolationException ex) {
            siemAuditService.registrarEvento(
                    "REGISTRO_CLIENTE",
                    dto.getUsuario() != null ? dto.getUsuario() : "Nuevo",
                    "Usuarios",
                    "Fallido",
                    "Registro fallido — datos duplicados: " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                    "ADVERTENCIA"
            );
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Datos duplicados o inválidos",
                    "error", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
            ));
        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "REGISTRO_CLIENTE",
                    dto.getUsuario() != null ? dto.getUsuario() : "Nuevo",
                    "Usuarios",
                    "Error",
                    "Error inesperado al registrar cliente: " + e.getMessage(),
                    "ALERTA"
            );
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error al crear cliente",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping({"/crear-usuario", "/crear_usuario"})
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioDTO dto) {
        try {
            if (dto.getIdRol() == null) {
                siemAuditService.registrarEvento(
                        "CREAR_USUARIO",
                        dto.getUsuario() != null ? dto.getUsuario() : "Admin",
                        "Usuarios",
                        "Denegado",
                        "Intento de crear usuario sin especificar rol.",
                        "ADVERTENCIA"
                );
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "idRol es obligatorio para crear usuario"
                ));
            }

            usuarioService.crearUsuarioConSP(dto);

            siemAuditService.registrarEvento(
                    "CREAR_USUARIO",
                    dto.getUsuario() != null ? dto.getUsuario() : "Admin",
                    "Usuarios",
                    "Exitoso",
                    "Nuevo usuario creado: \"" + dto.getUsuario() + "\" con rol ID=" + dto.getIdRol() + ".",
                    "INFO"
            );

            return ResponseEntity.ok(Map.of("success", true, "message", "Usuario creado"));
        } catch (DataIntegrityViolationException ex) {
            siemAuditService.registrarEvento(
                    "CREAR_USUARIO",
                    dto.getUsuario() != null ? dto.getUsuario() : "Admin",
                    "Usuarios",
                    "Fallido",
                    "Creación fallida — datos duplicados: " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                    "ADVERTENCIA"
            );
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Datos duplicados o inválidos",
                    "error", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
            ));
        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "CREAR_USUARIO",
                    dto.getUsuario() != null ? dto.getUsuario() : "Admin",
                    "Usuarios",
                    "Error",
                    "Error al crear usuario: " + e.getMessage(),
                    "ALERTA"
            );
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error al crear usuario",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/cliente/{id}")
    public ResponseEntity<?> actualizarCliente(
            @PathVariable Integer id,
            @RequestBody ClienteUpdateRequest dto) {

        usuarioService.actualizarCliente(id, dto);

        siemAuditService.registrarEvento(
                "ACTUALIZAR_CLIENTE",
                String.valueOf(id),
                "Usuarios",
                "Exitoso",
                "Actualizó los datos del cliente ID=" + id + ".",
                "INFO"
        );

        return ResponseEntity.ok(Map.of("success", true, "message", "Cliente actualizado"));
    }

    @GetMapping("/api/usuarios/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        Integer userId = resolveUserId(req);
        var u = usuarioService.getById(userId);

        siemAuditService.registrarEvento(
                "PERFIL_VER",
                String.valueOf(userId),
                "Usuarios",
                "Exitoso",
                "Consultó su perfil de usuario.",
                "INFO"
        );

        var payload = Map.of(
                "usuarioId", u.getUsuarioId(),
                "usuario", u.getUsuario(),
                "nombre", u.getNombre(),
                "correo", u.getCorreo(),
                "telefono", u.getTelefono(),
                "id_rol", u.getIdRol()
        );
        return ResponseEntity.ok(Map.of("data", payload));
    }

    /* ===== ADMIN/TRABAJADOR ===== */

    @PostMapping("/crear-usuarioAdmin")
    public ResponseEntity<Void> crearAdminOTrabajador(@Valid @RequestBody UsuarioDTO dto) {
        usuarioService.crearAdminOTrabajador(dto);

        siemAuditService.registrarEvento(
                "CREAR_USUARIO_ADMIN",
                dto.getUsuario() != null ? dto.getUsuario() : "Admin",
                "Usuarios",
                "Exitoso",
                "Admin creó nuevo usuario: \"" + dto.getUsuario() + "\" con rol ID=" + dto.getIdRol() + ".",
                "INFO"
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Void> actualizarAdmin(
            @PathVariable Integer id,
            @RequestParam Integer rolId,
            @Valid @RequestBody ClienteUpdateRequest req) {

        usuarioService.actualizarAdmin(id, rolId, req);

        siemAuditService.registrarEvento(
                "ACTUALIZAR_USUARIO_ADMIN",
                String.valueOf(id),
                "Usuarios",
                "Exitoso",
                "Admin actualizó el usuario ID=" + id + " (rol ID=" + rolId + ").",
                "INFO"
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deshabilitarAdmin(
            @PathVariable Integer id,
            @RequestParam Integer rolId) {

        usuarioService.deshabilitarAdmin(id, rolId);

        siemAuditService.registrarEvento(
                "DESHABILITAR_USUARIO",
                String.valueOf(id),
                "Usuarios",
                "Exitoso",
                "Admin deshabilitó al usuario ID=" + id + " (rol ID=" + rolId + ").",
                "ADVERTENCIA"
        );

        return ResponseEntity.ok().build();
    }

    /* ===== Búsqueda de usuarios ===== */

    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarPorUsuario(
            @RequestParam("usuario") String usuario,
            @RequestParam(value = "rolId", required = false) Integer rolId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        var resultados = usuarioService.buscarPorUsuario(usuario, rolId, limit);

        siemAuditService.registrarEvento(
                "BUSQUEDA_USUARIO",
                "Admin",
                "Usuarios",
                "Exitoso",
                "Buscó usuarios con término=\"" + usuario + "\" (rol=" + rolId + ") — " + resultados.size() + " resultado(s).",
                "INFO"
        );

        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/buscar-min")
    public ResponseEntity<List<UsuarioMinDTO>> buscarMin(
            @RequestParam("q") String q,
            @RequestParam(value = "rolId", required = false) Integer rolId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        return ResponseEntity.ok(usuarioService.buscarMin(q, rolId, limit));
    }

    /** Obtiene el userId desde la cabecera X-User-Id (fallback: sesión) */
    private Integer resolveUserId(HttpServletRequest req) {
        String hdr = req.getHeader("X-User-Id");
        if (hdr != null && !hdr.isBlank()) {
            try { return Integer.valueOf(hdr); } catch (NumberFormatException ignore) {}
        }
        var ses = req.getSession(false);
        Object attr = (ses != null) ? ses.getAttribute("userId") : null;
        if (attr instanceof Integer i) return i;
        if (attr instanceof String s) try { return Integer.valueOf(s); } catch (Exception ignore) {}
        throw new IllegalArgumentException("No se pudo resolver el usuario actual");
    }
}
