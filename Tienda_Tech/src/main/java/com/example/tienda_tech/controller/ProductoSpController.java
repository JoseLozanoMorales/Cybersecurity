package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.dto.ProductoUpdateBasicoRequest;
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
            @Valid @RequestBody AlmacenamientoCreateRequest req,@RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearAlmacenamientoJsonV2(req,usuario); // llama al SP v2 JSON
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/cpu")
    public ResponseEntity<?> crearCpu(
        @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
        com.example.tienda_tech.dto.CpuCreateRequest req,@RequestHeader(value = "X-Usuario", required = false) String usuario
        ) {
        service.crearCpuJsonV2(req, usuario);
        // <- ahora usa el SP v2 JSON
    return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }

    @PostMapping("/cpu-cooler")
    public org.springframework.http.ResponseEntity<?> crearCpuCooler(
        @jakarta.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        com.example.tienda_tech.dto.CpuCoolerCreateRequest req, @RequestHeader(value = "X-Usuario", required = false) String usuario) {

    service.crearCpuCoolerJsonV2(req,usuario);
    return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/cubiertas")
    public org.springframework.http.ResponseEntity<?> crearCubierta(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.CubiertaCreateRequest req, @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearCubiertaJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/fuentes")
    public org.springframework.http.ResponseEntity<?> crearFuente(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.FuenteCreateRequest req, @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearFuenteJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/gpu")
    public org.springframework.http.ResponseEntity<?> crearGpu(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.GpuCreateRequest req,@RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearGpuJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/ram")
    public org.springframework.http.ResponseEntity<?> crearRam(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.RamCreateRequest req,@RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearRamJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/motherboards")
    public org.springframework.http.ResponseEntity<?> crearMotherboard(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.MotherboardCreateRequest req,@RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearMotherboardJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    @PostMapping("/perifericos")
    public org.springframework.http.ResponseEntity<?> crearPeriferico(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.example.tienda_tech.dto.PerifericoCreateRequest req, @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.crearPerifericoJsonV2(req,usuario);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    /*
    @PutMapping("/productos/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Integer id,
            @RequestBody com.example.tienda_tech.dto.ProductoUpdateRequest req) {

        service.actualizarProductoJsonV2(id, req);
        return ResponseEntity.ok(java.util.Map.of("ok", true));
    }
    */
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        service.eliminarProductoJsonV2(id, usuario);
        // El SP decide: hard delete o habilitado=false. Con 200 OK basta.
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/productos/{id}/editar") // <-- SIN el /sp extra
    public ResponseEntity<?> getDetalleEditar(@PathVariable Integer id) {
        return service.detalleParaEditarOpt(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message","Producto no encontrado")));
    }

    // GET IVAs
    @GetMapping("/ivas")
    public ResponseEntity<?> getIvas() {
        return ResponseEntity.ok(service.listarIvas());
    }

    // PUT actualizar básico
    @PutMapping("/productos/{id}/basico")
    public ResponseEntity<?> actualizarBasico(
            @PathVariable Integer id,
            @RequestBody ProductoUpdateBasicoRequest req,
            @RequestHeader(value="X-Usuario", required=false) String usuario) {
        service.actualizarProductoBasico(id, req, usuario);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
