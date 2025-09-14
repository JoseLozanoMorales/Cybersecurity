package com.example.tienda_tech.controller;


import com.example.tienda_tech.dto.ProductoListDTO;
import com.example.tienda_tech.dto.TopVendidoDto;
import com.example.tienda_tech.service.ProductoService;
import com.example.tienda_tech.service.TopVentasService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.tienda_tech.dto.TopVendidoDto;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService service;
    private final TopVentasService topVentasService;


    // GET /api/productos?page=0&size=50
    @GetMapping
    public Page<ProductoListDTO> listar(@PageableDefault(size = 50) Pageable pageable){
    return service.listar(pageable);
    }
    @GetMapping("/mas-vendidos")
    public List<TopVendidoDto> masVendidos(@RequestParam(defaultValue = "3") int limite){
        return topVentasService.listar(limite);
    }
}