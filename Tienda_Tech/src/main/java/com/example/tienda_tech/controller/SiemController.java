package com.example.tienda_tech.controller;

import com.example.tienda_tech.model.SiemEvent;
import com.example.tienda_tech.service.SiemAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/siem")
public class SiemController {

    @Autowired
    private SiemAuditService siemAuditService;

    @GetMapping("/events")
    public ResponseEntity<List<SiemEvent>> getEvents() {
        return ResponseEntity.ok(siemAuditService.obtenerEventos());
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearEvents() {
        siemAuditService.limpiarEventos();
        return ResponseEntity.ok(Map.of("success", true, "message", "Historial de auditoria SIEM limpiado."));
    }
}
