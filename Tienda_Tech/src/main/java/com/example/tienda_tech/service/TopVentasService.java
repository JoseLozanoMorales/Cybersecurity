package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.TopVendidoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopVentasService {

  private final JdbcTemplate jdbc;

  public List<TopVendidoDto> listar(int limite){
    return jdbc.query(
      "SELECT * FROM public.productos_mas_vendidos_menu(?)",
      ps -> ps.setInt(1, limite),
      (rs, i) -> new TopVendidoDto(
        rs.getInt("producto_id"),
        rs.getString("nombre"),
        rs.getBigDecimal("precio"),
        rs.getLong("ventas"),
        (Integer) rs.getObject("galeria_id"),
        rs.getString("url_imagen")
      )
    );
  }
}
