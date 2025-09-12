package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubtipoMovimientoService {

    private final JdbcTemplate jdbc;

    public List<Map<String, Object>> listarSubtipos(Integer tipo) throws Exception {
        final String sql = "SELECT * FROM public.fn_subtipos_movimiento(?)";

        try (Connection cn = jdbc.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            if (tipo == null) {
                ps.setNull(1, Types.INTEGER);   // 👈 clave: NULL tipado
            } else {
                ps.setInt(1, tipo);
            }

            List<Map<String, Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("subtipo_id", rs.getInt("subtipo_id"));
                    row.put("nombre", rs.getString("nombre"));
                    row.put("tipo_id", rs.getInt("tipo_id"));
                    out.add(row);
                }
            }
            return out;
        }
    }
}
