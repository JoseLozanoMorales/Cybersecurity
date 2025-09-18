// src/main/java/com/example/tienda_tech/controller/UsuarioController.java
package com.example.tienda_tech.controller;
import com.example.tienda_tech.dto.UsuarioMinDTO;
import com.example.tienda_tech.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;   // <-- NUEVO (no javax)
import org.springframework.http.ResponseEntity;

import com.example.tienda_tech.dto.UsuarioDTO;
import com.example.tienda_tech.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
// ...los que ya tengas
import org.springframework.web.bind.annotation.*;
import com.example.tienda_tech.dto.ClienteUpdateRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {


    @Autowired
    private com.example.tienda_tech.service.UsuarioService usuarioService;

    /**
     * Registro público (self-service) de clientes.
     * Invoca SP: crear_cliente(...)
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearCliente(@RequestBody UsuarioDTO dto) {
        try {
            usuarioService.crearClienteConSP(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cliente creado"
            ));
        } catch (DataIntegrityViolationException ex) {
            // UNIQUE violations (usuario/correo/cedula), FK, etc.
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Datos duplicados o inválidos",
                    "error", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
            ));
        } catch (Exception e) {
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
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "idRol es obligatorio para crear usuario"
                ));
            }
            usuarioService.crearUsuarioConSP(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuario creado"
            ));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Datos duplicados o inválidos",
                    "error", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
            ));
        } catch (Exception e) {
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
        return ResponseEntity.ok(Map.of("success", true, "message", "Cliente actualizado"));
    }

    @GetMapping("/api/usuarios/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        Integer userId = resolveUserId(req);               // lee X-User-Id
        var u = usuarioService.getById(userId);            // usa el service
        var payload = java.util.Map.of(
            "usuarioId", u.getUsuarioId(),
            "usuario",   u.getUsuario(),
            "nombre",    u.getNombre(),
            "correo",    u.getCorreo(),
            "telefono",  u.getTelefono(),
            "id_rol",    u.getIdRol()
        );
        return ResponseEntity.ok(java.util.Map.of("data", payload));
    }

    /** Obtiene el userId desde la cabecera X-User-Id (fallback: sesión) */
    private Integer resolveUserId(HttpServletRequest req){
        String hdr = req.getHeader("X-User-Id");
        if (hdr != null && !hdr.isBlank()) {
            try { return Integer.valueOf(hdr); } catch (NumberFormatException ignore) {}
        }
        var ses = req.getSession(false);
        Object attr = (ses != null) ? ses.getAttribute("userId") : null;
        if (attr instanceof Integer i) return i;
        if (attr instanceof String s)  try { return Integer.valueOf(s); } catch (Exception ignore) {}
        throw new IllegalArgumentException("No se pudo resolver el usuario actual");
    }

    /* ===== ADMIN/TRABAJADOR – nuevo flujo JSON ===== */
    @PostMapping("/crear-usuarioAdmin")
    public ResponseEntity<Void> crearAdminOTrabajador(@Valid @RequestBody UsuarioDTO dto) {
        usuarioService.crearAdminOTrabajador(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Void> actualizarAdmin(
            @PathVariable Integer id,
            @RequestParam Integer rolId,
            @Valid @RequestBody ClienteUpdateRequest req) {
        usuarioService.actualizarAdmin(id, rolId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deshabilitarAdmin(
            @PathVariable Integer id,
            @RequestParam Integer rolId) {
        usuarioService.deshabilitarAdmin(id, rolId);
        return ResponseEntity.ok().build();
    }

    /* ===== Búsqueda (sin searchMin) ===== */
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarPorUsuario(
            @RequestParam("usuario") String usuario,
            @RequestParam(value = "rolId", required = false) Integer rolId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(usuarioService.buscarPorUsuario(usuario, rolId, limit));
    }

    // NUEVO: búsqueda mínima para autocompletar/seleccionar
    @GetMapping("/buscar-min")
    public ResponseEntity<List<UsuarioMinDTO>> buscarMin(
            @RequestParam("q") String q,
            @RequestParam(value = "rolId", required = false) Integer rolId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(usuarioService.buscarMin(q, rolId, limit));
    }

}
