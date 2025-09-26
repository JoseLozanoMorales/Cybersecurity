package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.BusquedaCardDto;
import com.example.tienda_tech.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService service;

    // GET /api/busqueda/categoria/123
    @GetMapping("/categoria/{categoriaId}")
    public List<BusquedaCardDto> porCategoria(@PathVariable Integer categoriaId){
        return service.porCategoria(categoriaId);
    }
}
