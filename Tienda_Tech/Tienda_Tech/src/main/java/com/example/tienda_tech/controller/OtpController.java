package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.OtpService;
import com.example.tienda_tech.service.OtpService.OtpTooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping
    public ResponseEntity<?> manejar(@RequestBody Map<String, String> body) {
        String accion = safe(body.get("accion")).toLowerCase();
        String correo = safe(body.get("correo"));

        try {
            switch (accion) {
                case "enviar" -> {
                    String txId = body.get("txId"); // opcional
                    var resp = otpService.enviar(correo, txId);
                    return ResponseEntity.ok(resp);
                }
                case "validar" -> {
                    String codigo = safe(body.get("codigo"));
                    String txId = safe(body.get("txId"));
                    boolean ok = otpService.validar(correo, codigo, txId);
                    if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código inválido o expirado");
                    return ResponseEntity.ok().build();
                }
                default -> { return ResponseEntity.badRequest().body("accion debe ser 'enviar' o 'validar'"); }
            }
        } catch (OtpTooManyRequestsException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando OTP: " + e.getMessage());
        }
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
