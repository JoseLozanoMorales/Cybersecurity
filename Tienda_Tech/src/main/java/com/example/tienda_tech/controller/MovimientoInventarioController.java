package com.example.tienda_tech.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final JdbcTemplate jdbc;

    @GetMapping("/api/movimientos")
    public List<Map<String, Object>> listar() {
        // Llama a tu función set-returning (RETURNS TABLE)
        final String sql = "SELECT * FROM public.fn_mostrar_movimientos_inventario()";
        return jdbc.queryForList(sql);
    }
}
