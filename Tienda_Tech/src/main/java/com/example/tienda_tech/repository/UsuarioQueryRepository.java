package com.example.tienda_tech.repository;

import com.example.tienda_tech.dto.UsuarioMinDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsuarioQueryRepository {
    private final JdbcTemplate jdbc;

    public List<UsuarioMinDTO> buscarMin(String q, Integer rolId, int limit) {
        String sql = "SELECT * FROM public.f_buscar_usuarios_min(?,?,?)";
        return jdbc.query(sql, ps -> {
            ps.setString(1, q == null ? "" : q.trim());
            if (rolId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, rolId);
            ps.setInt(3, limit <= 0 ? 20 : Math.min(limit, 50));
        }, (rs, i) -> new UsuarioMinDTO(
                rs.getInt("usuario_id"),
                rs.getString("nombre"),
                rs.getString("cedula"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("usuario"),
                rs.getInt("rol_id"),
                rs.getBoolean("habilitado")
        ));
    }
}

