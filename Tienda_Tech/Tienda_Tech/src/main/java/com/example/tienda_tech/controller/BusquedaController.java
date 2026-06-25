package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.BusquedaCardDto;
import com.example.tienda_tech.service.BusquedaService;
import com.example.tienda_tech.service.SiemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService service;
    private final SiemAuditService siemAuditService;

    // GET /api/busqueda/categoria/123
    @GetMapping("/categoria/{categoriaId}")
    public List<BusquedaCardDto> porCategoria(
            @PathVariable Integer categoriaId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        String usuario = userId != null ? String.valueOf(userId) : "Anónimo";

        List<BusquedaCardDto> resultados = service.porCategoria(categoriaId);

        siemAuditService.registrarEvento(
                "FILTRO_CATEGORIA",
                usuario,
                "Búsqueda",
                "Exitoso",
                "Filtró por categoría ID=" + categoriaId + " — " + resultados.size() + " producto(s).",
                "INFO"
        );

        return resultados;
    }
}
