package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.BusquedaCardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusquedaService {
    private final JdbcTemplate jdbc;

    public List<BusquedaCardDto> porCategoria(Integer categoriaId){
        String sql = "SELECT * FROM public.f_busqueda_por_categoria(?)";
        return jdbc.query(sql, (rs, i) -> new BusquedaCardDto(
                rs.getInt("producto_id"),
                rs.getString("nombre"),
                rs.getBigDecimal("preciounitario"),
                rs.getString("marca"),
                (Integer) rs.getObject("imagen_id")
        ), categoriaId);
    }
}
