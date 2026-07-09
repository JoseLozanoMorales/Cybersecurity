package com.example.tienda_tech.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class SubtipoMovimientoRepository {

  private final JdbcTemplate jdbc;

  public SubtipoMovimientoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public List<SubtipoRow> listar(Integer tipo /* puede ser null */) {
    final String sql = "SELECT * FROM public.fn_subtipos_movimiento(?::integer)";
    return jdbc.query(sql, ps -> {
      if (tipo == null) ps.setNull(1, Types.INTEGER);
      else ps.setInt(1, tipo);
    }, (rs, i) -> new SubtipoRow(
        rs.getInt("subtipo_id"),
        rs.getString("nombre"),
        (Integer) rs.getObject("tipo_id")
    ));
  }

  public record SubtipoRow(Integer subtipo_id, String nombre, Integer tipo_id) {}
}
