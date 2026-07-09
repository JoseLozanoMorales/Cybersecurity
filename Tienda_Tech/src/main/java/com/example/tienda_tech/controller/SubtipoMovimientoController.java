package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.SubtipoMovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SubtipoMovimientoController {

    private final SubtipoMovimientoService service;

    @GetMapping("/api/subtipos-movimiento")
    public List<Map<String, Object>> listar(@RequestParam(name = "tipo", required = false) Integer tipo) throws Exception {
        // tipo: 1=Entrada, 2=Salida, null = todos
        return service.listarSubtipos(tipo);
    }
}
