package com.example.tienda_tech.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om = new ObjectMapper();

    public Map<String,Object> confirmarOrdenDesdeCarrito(Integer usuarioId, Integer direccionId, Integer metodopagoId) {
        String json = om.createObjectNode()
                .put("usuarioId",    usuarioId)
                .put("direccionId",  direccionId)
                .put("metodopagoId", metodopagoId)
                .toString();

        // Llamamos a la FUNCIÓN (no procedure)
        String sql = "select * from public.f_checkout_generar_orden_json(?::jsonb)";

        Map<String,Object> row = jdbc.queryForMap(sql, json);

        return Map.of(
                "ordenId",   ((Number) row.get("orden_id")).intValue(),
                "subtotal",  row.get("subtotal"),
                "impuestos", row.get("impuestos"),
                "total",     row.get("total")
        );
    }
}