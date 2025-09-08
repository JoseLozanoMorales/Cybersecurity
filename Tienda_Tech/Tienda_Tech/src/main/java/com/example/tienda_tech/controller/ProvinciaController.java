// src/main/java/com/example/tienda_tech/controller/ProvinciaController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.ProvinciaDTO;
import com.example.tienda_tech.service.ProvinciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/provincias")// opcional si sirves el HTML fuera de Spring
public class ProvinciaController {

    private final ProvinciaService svc;

    @GetMapping
    public List<ProvinciaDTO> listar() {
        return svc.listar();
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody ProvinciaDTO dto) {
        svc.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody ProvinciaDTO dto) {
        svc.actualizar(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        svc.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
