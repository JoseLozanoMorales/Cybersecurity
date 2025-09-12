package com.example.tienda_tech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoBusquedaController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om = new ObjectMapper();

    @PostMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscar(@RequestBody(required = false) Map<String, Object> filtros) throws Exception {
        // Si el body viene vacío, usa {}
        if (filtros == null) filtros = Map.of();

        // Tu función: fn_buscar_productos_json(jsonb)
        final String sql = "SELECT * FROM public.fn_buscar_productos_json(?::jsonb)";
        final String json = om.writeValueAsString(filtros);

        List<Map<String, Object>> rows = jdbc.queryForList(sql, json);
        return ResponseEntity.ok(rows);
    }
}
