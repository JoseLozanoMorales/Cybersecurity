package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.CarritoAddReq;
import com.example.tienda_tech.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService service;

    // Usa el header que ya inyecta tu auth-menu.js (X-User-Id).
    private Integer resolveUserId(Integer headerUserId){
        if (headerUserId == null) throw new RuntimeException("Usuario no autenticado");
        return headerUserId;
    }

    // Agregar (lo llama informacion_producto.html)
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
                                     @RequestBody CarritoAddReq req){
        Integer uid = resolveUserId(uidHdr);
        int qty = Math.max(1, req.getCantidad()==null?1:req.getCantidad());
        service.agregar(uid, req.getProductoId(), qty);
        return ResponseEntity.ok("{\"ok\":true}");
    }

    // Alias por compatibilidad con tu fallback
    @PostMapping("/agregar")
    public ResponseEntity<?> addItemAlias(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
                                          @RequestBody CarritoAddReq req){
        return addItem(uidHdr, req);
    }

    // Listar ítems (lo usará Carrito.html)
    @GetMapping("/items")
    public ResponseEntity<?> list(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr){
        Integer uid = resolveUserId(uidHdr);
        return ResponseEntity.ok(service.listarItems(uid));
    }

    // Resumen (subtotal/iva/total)
    @GetMapping("/resumen")
    public ResponseEntity<?> summary(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr){
        Integer uid = resolveUserId(uidHdr);
        return ResponseEntity.ok(service.resumen(uid));
    }

    // Cambiar cantidad
    @PatchMapping("/items/{productoId}")
    public ResponseEntity<?> setQty(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
                                    @PathVariable Integer productoId,
                                    @RequestBody java.util.Map<String,Integer> body){
        Integer uid = resolveUserId(uidHdr);
        int qty = Math.max(1, body.getOrDefault("cantidad",1));
        service.setCantidad(uid, productoId, qty);
        return ResponseEntity.ok("{\"ok\":true}");
    }

    // Eliminar
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<?> remove(@RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
                                    @PathVariable Integer productoId){
        Integer uid = resolveUserId(uidHdr);
        service.quitar(uid, productoId);
        return ResponseEntity.ok("{\"ok\":true}");
    }
}
