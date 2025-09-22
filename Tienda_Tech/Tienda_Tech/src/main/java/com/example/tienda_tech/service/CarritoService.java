package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarritoService {
    private final JdbcTemplate jdbc;

    // en CarritoService
    public void agregar(Integer uid, Integer pid, Integer qty){
        jdbc.queryForObject( // consume el result set de la FUNCTION
                "SELECT public.f_carrito_agregar(?,?,?)",
                (rs, rowNum) -> null, uid, pid, qty
        );
    }
    public void setCantidad(Integer uid, Integer pid, Integer qty){
        jdbc.queryForObject("SELECT public.f_carrito_set_qty(?,?,?)",(rs,r)->null, uid,pid,qty);
    }
    public void quitar(Integer uid, Integer pid){
        jdbc.queryForObject("SELECT public.f_carrito_quitar(?,?)",(rs,r)->null, uid,pid);
    }

    public List<Map<String, Object>> listarItems(Integer usuarioId) {
        return jdbc.queryForList("SELECT * FROM public.f_carrito_items(?)", usuarioId);
    }

    public Map<String,Object> resumen(Integer uid){
        return jdbc.queryForMap("SELECT * FROM public.f_carrito_resumen(?)", uid);
    }
}
