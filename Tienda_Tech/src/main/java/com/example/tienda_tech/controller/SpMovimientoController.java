// src/main/java/com/example/tienda_tech/controller/SpMovimientoController.java
package com.example.tienda_tech.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.CallableStatement;

@RestController
@RequestMapping("/api/sp")
public class SpMovimientoController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public SpMovimientoController(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    /**
     * Acepta body como objeto o arreglo JSON.
     * Puedes pasar ?usuario=jlozano; si no, toma el del SecurityContext.
     * Si queda null, el SP intentará usar item.usuario.
     */
    @PostMapping("/movimiento-inventario")
    public ResponseEntity<Void> movimientoInventario(
            @RequestBody Object body,
            @RequestParam(value = "usuario", required = false) String usuarioParam
    ) {
        // intenta tomar del SecurityContext si no vino por query param
        String usuario = (usuarioParam != null && !usuarioParam.isBlank())
                ? usuarioParam
                : resolveUserFromSecurity();

        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (CallableStatement cs = con.prepareCall(
                    "CALL public.sp_movimiento_inventario_json(?::jsonb, ?)")) {
                cs.setString(1, om.writeValueAsString(body)); // JSON tal cual (objeto o array)
                if (usuario == null || usuario.isBlank()) {
                    cs.setNull(2, java.sql.Types.VARCHAR);     // deja que el SP use item.usuario
                } else {
                    cs.setString(2, usuario);
                }
                cs.execute();
                return null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.noContent().build();
    }

    private String resolveUserFromSecurity() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName(); // username
            }
        } catch (Exception ignore) {}
        return null;
    }
}
