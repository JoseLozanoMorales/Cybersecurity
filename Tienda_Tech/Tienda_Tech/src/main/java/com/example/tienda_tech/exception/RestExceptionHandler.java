package com.example.tienda_tech.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> bad(BadRequestException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> conflict(ConflictException e) {
        return ResponseEntity.status(409).body(Map.of("success", false, "error", e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> di(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Map.of("success", false, "error", "Conflicto con datos (restricción de unicidad o FK)."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception e) {
        return ResponseEntity.status(500).body(Map.of("success", false, "error", "Error interno", "detail", e.getMessage()));
    }
}
