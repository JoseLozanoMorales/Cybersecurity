package com.example.tienda_tech.repository;

import com.example.tienda_tech.dto.ProductoRecienteMenuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductoRecientesRepo {

    private final JdbcTemplate jdbc;

    public List<ProductoRecienteMenuDto> listar(int limit) {
        final String sql = """
      select producto_id, nombre, precio, fecha, galeria_id, mime_type
      from public.f_productos_recientes_con_imagen_menu(?)
      """;
        return jdbc.query(sql, ps -> ps.setInt(1, limit), (rs, i) ->
                new ProductoRecienteMenuDto(
                        rs.getInt("producto_id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getLong("galeria_id"),
                        rs.getString("mime_type")
                )
        );
    }
}
