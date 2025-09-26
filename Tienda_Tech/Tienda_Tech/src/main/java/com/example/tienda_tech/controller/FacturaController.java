package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.FacturaPdfService;
import com.example.tienda_tech.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturas;
    private final FacturaPdfService pdfs;   // 👈

    @GetMapping("/{id}")
    public Map<String,Object> get(@PathVariable Integer id){
        return facturas.obtenerFactura(id);
    }

    @GetMapping(value="/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable Integer id){
        byte[] pdf = pdfs.render(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura-"+id+".pdf")
                .body(pdf);
    }
}