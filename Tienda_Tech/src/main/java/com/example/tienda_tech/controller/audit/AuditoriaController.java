package com.example.tienda_tech.controller.audit;

import com.example.tienda_tech.dto.audit.*;
import com.example.tienda_tech.service.audit.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping("/usuarios")
    public List<String> listarUsuarios() {
        return auditoriaService.listarUsuarios();
    }

    @GetMapping("/productos")
    public List<ProductoAuditoriaDTO> productos(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return auditoriaService.buscarProductos(usuario, productoId, desde, hasta);
    }

    @GetMapping("/movimientos")
    public List<MovimientoAuditoriaDTO> movimientos(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return auditoriaService.buscarMovimientos(usuario, productoId, desde, hasta);
    }

    @GetMapping("/usuario")
    public UsuarioAuditoriaRespuestaDTO porUsuario(
            @RequestParam String usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return auditoriaService.auditoriaPorUsuario(usuario, desde, hasta);
    }
}
