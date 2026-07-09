package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.CheckoutResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om = new ObjectMapper();

    public CheckoutResult confirmarOrdenDesdeCarrito(Integer usuarioId,
                                                     Integer direccionId,
                                                     Integer metodopagoId) throws JsonProcessingException {
        try {
            var payload = om.writeValueAsString(Map.of(
                    "usuarioId", usuarioId,
                    "direccionId", direccionId,
                    "metodopagoId", metodopagoId
            ));
            System.out.printf("[PAY] uid=%s dir=%s mp=%s%n", usuarioId, direccionId, metodopagoId);

            var rows = jdbc.queryForList(
                    "select * from public.f_checkout_generar_orden_json(?::jsonb)", payload);

            if (rows.isEmpty()) {
                throw new IllegalStateException("f_checkout_generar_orden_json devolvió 0 filas. " +
                        "Revisa carrito/dirección/método de pago.");
            }

            var r = rows.get(0);
            return new CheckoutResult(
                    ((Number) r.get("orden_id")).intValue(),
                    new BigDecimal(r.get("subtotal").toString()),
                    new BigDecimal(r.get("impuestos").toString()),
                    new BigDecimal(r.get("total").toString()),
                    ((Number) r.get("factura_id")).intValue(),
                    String.valueOf(r.get("factura_numero"))
            );
        } catch (RuntimeException | JsonProcessingException e) {
            // Log detallado para depurar
            e.printStackTrace();
            throw e;
        }
    }
}
