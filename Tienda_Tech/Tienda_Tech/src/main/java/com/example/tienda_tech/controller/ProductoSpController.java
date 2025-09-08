package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.service.ProductoSpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // <<--- FALTABAAAA :D

@RestController
@RequestMapping("/api/sp")
public class ProductoSpController {

    private final ProductoSpService service;

    public ProductoSpController(ProductoSpService service) {
        this.service = service;
    }

    @PostMapping("/almacenamientos")
    public ResponseEntity<?> crearAlmacenamiento(
            @Valid @RequestBody AlmacenamientoCreateRequest req) {

        service.crearAlmacenamientoJsonV2(req); // llama al SP v2 JSON
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/cpu")
    public ResponseEntity<?> crearCpu(
        @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
        com.example.tienda_tech.dto.CpuCreateRequest req) {

    service.crearCpuJsonV2(req);  // <- ahora usa el SP v2 JSON
    return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }

    @PostMapping("/cpu-cooler")
    public org.springframework.http.ResponseEntity<?> crearCpuCooler(
        @jakarta.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        com.example.tienda_tech.dto.CpuCoolerCreateRequest req) {

    service.crearCpuCoolerJsonV2(req);
    return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/cubiertas")
    public org.springframework.http.ResponseEntity<?> crearCubierta(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.CubiertaCreateRequest req) {

        service.crearCubiertaJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/fuentes")
    public org.springframework.http.ResponseEntity<?> crearFuente(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.FuenteCreateRequest req) {

        service.crearFuenteJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/gpu")
    public org.springframework.http.ResponseEntity<?> crearGpu(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.GpuCreateRequest req) {

        service.crearGpuJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/ram")
    public org.springframework.http.ResponseEntity<?> crearRam(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.RamCreateRequest req) {

        service.crearRamJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/motherboards")
    public org.springframework.http.ResponseEntity<?> crearMotherboard(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.MotherboardCreateRequest req) {

        service.crearMotherboardJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/perifericos")
    public org.springframework.http.ResponseEntity<?> crearPeriferico(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.PerifericoCreateRequest req) {

        service.crearPerifericoJsonV2(req);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PutMapping("/productos/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Integer id,
            @RequestBody com.example.tienda_tech.dto.ProductoUpdateRequest req) {

        service.actualizarProductoJsonV2(id, req);
        return ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    

}
