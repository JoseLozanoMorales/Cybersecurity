package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.CheckoutResult;
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

    public CheckoutResult confirmarOrdenDesdeCarrito(Integer usuarioId, Integer direccionId, Integer metodopagoId) {
        String json = om.createObjectNode()
                .put("usuarioId",    usuarioId)
                .put("direccionId",  direccionId)
                .put("metodopagoId", metodopagoId)
                .toString();

        String sql = "select * from public.f_checkout_generar_orden_json(?::jsonb)";
        return jdbc.queryForObject(sql,
                (rs, i) -> new com.example.tienda_tech.dto.CheckoutResult(
                        rs.getInt("orden_id"),
                        rs.getBigDecimal("subtotal"),
                        rs.getBigDecimal("impuestos"),
                        rs.getBigDecimal("total"),
                        rs.getInt("factura_id"),
                        rs.getString("factura_numero")
                ),
                json
        );
    }
}