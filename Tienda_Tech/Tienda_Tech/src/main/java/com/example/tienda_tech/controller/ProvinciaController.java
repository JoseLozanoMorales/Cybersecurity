// src/main/java/com/example/tienda_tech/controller/ProvinciaController.java
package com.example.tienda_tech.controller;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.example.tienda_tech.repository.ProvinciaRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provincias")
@CrossOrigin // opcional si sirves el HTML fuera de Spring
public class ProvinciaController {

    private final ProvinciaRepository provinciaRepo;

    public ProvinciaController(ProvinciaRepository provinciaRepo) {
        this.provinciaRepo = provinciaRepo;
    }
    // ProvinciaController.java
    @GetMapping
    public List<Map<String, Object>> listar() {
        return provinciaRepo.findAll().stream()
            .map(p -> Map.<String, Object>of(
                "provinciaId", p.getProvinciaId(),
                "nombre",      p.getNombre()
            ))
            .collect(Collectors.toList());
    }

}
