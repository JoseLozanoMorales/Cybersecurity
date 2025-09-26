package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final JdbcTemplate jdbc;

    public Map<String,Object> obtenerFactura(Integer facturaId){
        var enc = jdbc.queryForMap(
                "select * from public.f_factura_enc(?)", facturaId);

        List<Map<String,Object>> det = jdbc.queryForList(
                "select * from public.f_factura_det(?)", facturaId);

        return Map.of("encabezado", enc, "detalle", det);
    }
}