package com.example.tienda_tech.controller;


import com.example.tienda_tech.dto.DireccionDTO;
import com.example.tienda_tech.service.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios/{usuarioId}/direcciones")
public class DireccionController {
    private final DireccionService service;

    // GET normal o detallado (con ?view=full)
    @GetMapping
    public List<DireccionDTO> listar(@PathVariable Integer usuarioId,
                                     @RequestParam(value="view", required=false) String view){
        if ("full".equalsIgnoreCase(view)) {
            return service.listarDetallado(usuarioId);   // ← calle/ref/ciudad/provincia
        }
        return service.listar(usuarioId);                // ← tu lista JPA de siempre
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DireccionDTO> crear(@PathVariable Integer usuarioId,
                                              @RequestBody DireccionDTO body){
        DireccionDTO creado = service.crear(usuarioId, body);
        return ResponseEntity.created(
                URI.create("/api/usuarios/" + usuarioId + "/direcciones/" + creado.getDireccionId())
        ).body(creado);
    }

    @PutMapping(value="/{direccionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DireccionDTO actualizar(@PathVariable Integer usuarioId,
                                   @PathVariable Short direccionId,     // <- Short
                                   @RequestBody DireccionDTO body){
        return service.actualizar(usuarioId, direccionId, body);
    }

    @DeleteMapping("/{direccionId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer usuarioId,
                                         @PathVariable Short direccionId){ // <- Short
        service.eliminar(usuarioId, direccionId);
        return ResponseEntity.noContent().build();
    }
}
