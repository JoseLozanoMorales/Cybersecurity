package com.example.tienda_tech.advice;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiErrors {

    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    // 1) Respeta estados lanzados explícitamente por los controladores
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> onRse(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason() != null ? ex.getReason() : "error"));
    }

    // 2) deleteById(...) de algo que no existe -> 404
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Void> onEmpty(EmptyResultDataAccessException ex) {
        return ResponseEntity.notFound().build();
    }

    // 3) Integridad: 409 solo en escrituras; en GET usa 500 (o ajusta si prefieres 404/403 según tu caso)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> onDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest req) {

        boolean isGet = "GET".equalsIgnoreCase(req.getMethod());
        HttpStatus status = isGet ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.CONFLICT;

        String msg = (ex.getMostSpecificCause() != null)
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        log.warn("[{} {}] DataIntegrityViolation -> {}", req.getMethod(), req.getRequestURI(), msg, ex);

        return ResponseEntity.status(status).body(Map.of(
                "error", msg,
                "detalle", "Violación de integridad de datos"
        ));
    }

    // 4) Otros errores de acceso a datos -> 500
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> onDataAccess(
            DataAccessException ex, HttpServletRequest req) {

        String msg = (ex.getMostSpecificCause() != null)
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        log.error("[{} {}] DataAccessException", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", msg));
    }

    // 5) Fallback genérico -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> onGeneric(
            Exception ex, HttpServletRequest req) {

        log.error("[{} {}] Unhandled exception", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno"));
    }
}
