// src/main/java/com/example/tienda_tech/controller/CiudadController.java
package com.example.tienda_tech.controller;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.example.tienda_tech.repository.CiudadRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin // opcional si sirves el HTML fuera de Spring
public class CiudadController {

    private final CiudadRepository ciudadRepo;

    public CiudadController(CiudadRepository ciudadRepo) {
        this.ciudadRepo = ciudadRepo;
    }

    // CiudadController.java
    @GetMapping("/ciudades")
    public List<Map<String, Object>> todas() {
        return ciudadRepo.findAll().stream()
            .map(c -> Map.<String, Object>of(
                "ciudadId",    c.getCiudadId(),
                "nombre",      c.getNombre(),
                "provinciaId", c.getProvinciaId()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/provincias/{provinciaId}/ciudades")
    public List<Map<String, Object>> porProvincia(@PathVariable Short provinciaId) {
        return ciudadRepo.findByProvinciaId(provinciaId).stream()
            .map(c -> Map.<String, Object>of(
                "ciudadId",    c.getCiudadId(),
                "nombre",      c.getNombre(),
                "provinciaId", c.getProvinciaId()
            ))
            .collect(Collectors.toList());
}

}
