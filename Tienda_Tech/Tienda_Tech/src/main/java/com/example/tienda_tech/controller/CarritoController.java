package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.CarritoAddReq;
import com.example.tienda_tech.service.CarritoService;
import com.example.tienda_tech.service.SiemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService service;
    private final SiemAuditService siemAuditService;

    private Integer resolveUserId(Integer headerUserId) {
        if (headerUserId == null) throw new RuntimeException("Usuario no autenticado");
        return headerUserId;
    }

    /** Agregar producto al carrito */
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr,
            @RequestBody CarritoAddReq req) {

        Integer uid = resolveUserId(uidHdr);
        int qty = Math.max(1, req.getCantidad() == null ? 1 : req.getCantidad());
        service.agregar(uid, req.getProductoId(), qty);

        siemAuditService.registrarEvento(
                "CARRITO_AGREGAR",
                String.valueOf(uid),
                "Carrito",
                "Exitoso",
                "Agregó producto ID=" + req.getProductoId() + " (cantidad: " + qty + ") al carrito.",
                "INFO"
        );

        return ResponseEntity.ok("{\"ok\":true}");
    }

    /** Alias de compatibilidad con el frontend anterior */
    @PostMapping("/agregar")
    public ResponseEntity<?> addItemAlias(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr,
            @RequestBody CarritoAddReq req) {
        return addItem(uidHdr, req);
    }

    /** Listar ítems del carrito */
    @GetMapping("/items")
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr) {

        Integer uid = resolveUserId(uidHdr);

        siemAuditService.registrarEvento(
                "CARRITO_VER",
                String.valueOf(uid),
                "Carrito",
                "Exitoso",
                "Consultó el contenido de su carrito de compras.",
                "INFO"
        );

        return ResponseEntity.ok(service.listarItems(uid));
    }

    /** Resumen (subtotal / IVA / total) */
    @GetMapping("/resumen")
    public ResponseEntity<?> summary(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr) {

        Integer uid = resolveUserId(uidHdr);

        siemAuditService.registrarEvento(
                "CARRITO_RESUMEN",
                String.valueOf(uid),
                "Carrito",
                "Exitoso",
                "Solicitó el resumen de totales del carrito.",
                "INFO"
        );

        return ResponseEntity.ok(service.resumen(uid));
    }

    /** Cambiar cantidad de un producto */
    @PatchMapping("/items/{productoId}")
    public ResponseEntity<?> setQty(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr,
            @PathVariable Integer productoId,
            @RequestBody java.util.Map<String, Integer> body) {

        Integer uid = resolveUserId(uidHdr);
        int qty = Math.max(1, body.getOrDefault("cantidad", 1));
        service.setCantidad(uid, productoId, qty);

        siemAuditService.registrarEvento(
                "CARRITO_ACTUALIZAR",
                String.valueOf(uid),
                "Carrito",
                "Exitoso",
                "Actualizó cantidad del producto ID=" + productoId + " a " + qty + " unidad(es).",
                "INFO"
        );

        return ResponseEntity.ok("{\"ok\":true}");
    }

    /** Eliminar producto del carrito */
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<?> remove(
            @RequestHeader(value = "X-User-Id", required = false) Integer uidHdr,
            @PathVariable Integer productoId) {

        Integer uid = resolveUserId(uidHdr);
        service.quitar(uid, productoId);

        siemAuditService.registrarEvento(
                "CARRITO_ELIMINAR",
                String.valueOf(uid),
                "Carrito",
                "Exitoso",
                "Eliminó el producto ID=" + productoId + " del carrito.",
                "INFO"
        );

        return ResponseEntity.ok("{\"ok\":true}");
    }
}
