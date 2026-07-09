package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.ProductoRecienteMenuDto;
import com.example.tienda_tech.service.ProductoRecientesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final ProductoRecientesService service;

    // GET /api/productos/recientes-menu?limit=5
    @GetMapping(value = "/productos/recientes-menu", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductoRecienteMenuDto> recientesMenu(
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit
    ) {
        return service.topRecientes(limit);
    }
}
