package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.service.AlmacenamientoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos/almacenamientos")
public class AlmacenamientoController {

    private final AlmacenamientoService service;

    public AlmacenamientoController(AlmacenamientoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody AlmacenamientoCreateRequest req) {
        service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
