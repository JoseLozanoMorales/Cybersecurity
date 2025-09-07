package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.dto.CpuCreateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoSpServiceV1 {

    private final JdbcTemplate jdbc;

    // Constructor explícito (sin Lombok)
    public ProductoSpServiceV1(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void crearAlmacenamiento(AlmacenamientoCreateRequest r) {
        // Ajusta la lista de ? si tu SP tiene más/menos parámetros
        jdbc.update(
            "CALL public.agregar_almacenamiento(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            r.getNombre(),
            r.getPreciounitario(),
            r.getEnlace(),
            r.getStock(),
            r.getMarca_id(),
            r.getGama_id(),
            r.getIva_id(),
            r.getCosto(),
            r.getCapacidad(),
            r.getTipo()
        );
    }

    @Transactional
    public void crearCpu(CpuCreateRequest r) {
        jdbc.update(
            "CALL public.agregar_cpu(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            r.getNombre(),
            r.getPreciounitario(),
            r.getEnlace(),
            r.getStock(),
            r.getMarca_id(),
            r.getGama_id(),
            r.getIva_id(),
            r.getCosto(),
            r.getSockets(),
            r.getGeneracion()
        );
    }
}
