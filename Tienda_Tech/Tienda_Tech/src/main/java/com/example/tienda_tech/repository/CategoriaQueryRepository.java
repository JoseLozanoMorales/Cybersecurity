package com.example.tienda_tech.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoriaQueryRepository {

  private final JdbcTemplate jdbc;

  public CategoriaQueryRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // Devuelve filas tal cual vienen de la función
  public List<CategoriaRow> listar() {
    final String sql = "SELECT * FROM public.fn_listar_categorias()";
    return jdbc.query(sql, (rs, i) -> new CategoriaRow(
        rs.getInt("id_categoria"),
        rs.getString("nombre")
    ));
  }

  // DTO liviano para tu mapeo actual en el controlador
  public record CategoriaRow(Integer getId_categoria, String getNombre) {}
}
