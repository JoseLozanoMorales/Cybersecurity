package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.DireccionCreateRequest;
import com.example.tienda_tech.dto.DireccionDTO;
import com.example.tienda_tech.dto.DireccionUpdateRequest;
import com.example.tienda_tech.service.DireccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public List<DireccionDTO> listar(@PathVariable Integer usuarioId) {
        return direccionService.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<DireccionDTO> crear(@PathVariable Integer usuarioId,
                                            @RequestBody DireccionCreateRequest req) {
        return ResponseEntity.ok(direccionService.crear(usuarioId, req));
    }

    @PutMapping("/{direccionId}")
    public ResponseEntity<DireccionDTO> actualizar(@PathVariable Integer usuarioId,
                                                @PathVariable Integer direccionId,
                                                @RequestBody DireccionUpdateRequest req) {
        return ResponseEntity.ok(direccionService.actualizar(usuarioId, direccionId, req));
    }

    @DeleteMapping("/{direccionId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer usuarioId,
                                        @PathVariable Integer direccionId) {
        direccionService.eliminar(usuarioId, direccionId);
        return ResponseEntity.noContent().build();
    }

}
