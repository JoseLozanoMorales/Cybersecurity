package com.example.tienda_tech.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.CallableStatement;
import java.util.Map;

@RestController
@RequestMapping("/api/sp")
public class SpMovimientoController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public SpMovimientoController(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om   = om;
    }
    @PostMapping("/movimiento-inventario")
    public ResponseEntity<Void> movimientoInventario(@RequestBody Map<String, Object> body) {
        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (CallableStatement cs = con.prepareCall("CALL public.sp_movimiento_inventario_json(?::jsonb)")) {
                cs.setString(1, om.writeValueAsString(body)); // pasas el JSON tal cual
                cs.execute();
                return null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return ResponseEntity.noContent().build();
    }
}