// src/main/java/com/example/tienda_tech/controller/CiudadController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.CiudadDTO;
import com.example.tienda_tech.service.CiudadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ciudades")
public class CiudadController {
    private final CiudadService svc;

    @GetMapping
    public List<CiudadDTO> listar(){ return svc.listar(); }

    @PostMapping
    public ResponseEntity<Void> crear(@Validated @RequestBody CiudadDTO dto){
        svc.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Short id, @Validated @RequestBody CiudadDTO dto){
        svc.actualizar(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Short id){
        svc.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
