package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.MetodoPagoCreateRequest;
import com.example.tienda_tech.dto.MetodoPagoResponse;
import com.example.tienda_tech.dto.MetodoPagoUpdateRequest;
import com.example.tienda_tech.service.MetodoPagoService;
import com.example.tienda_tech.util.UserResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mis-metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService service;

    public MetodoPagoController(MetodoPagoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpServletRequest req) {
        Integer userId = UserResolver.resolveUserId(req);
        List<MetodoPagoResponse> data = service.listar(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody MetodoPagoCreateRequest body, HttpServletRequest req) {
        Integer userId = UserResolver.resolveUserId(req);
        service.agregar(userId, body);
        // puedes retornar el último insertado o solo OK
        List<MetodoPagoResponse> data = service.listar(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Método agregado", "data", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody MetodoPagoUpdateRequest body,
                                        HttpServletRequest req) {
        Integer userId = UserResolver.resolveUserId(req);
        service.actualizar(userId, id, body);
        List<MetodoPagoResponse> data = service.listar(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Método actualizado", "data", data));
    }

    @PutMapping("/{id}/preferido")
    public ResponseEntity<?> preferido(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = UserResolver.resolveUserId(req);
        service.marcarPreferido(userId, id);
        List<MetodoPagoResponse> data = service.listar(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Preferido actualizado", "data", data));
    }
}
