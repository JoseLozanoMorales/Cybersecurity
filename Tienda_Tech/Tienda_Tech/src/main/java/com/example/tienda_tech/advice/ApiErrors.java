package com.example.tienda_tech.advice;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {

  // Cuando se intenta borrar algo que no existe (deleteById de JPA)
  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ResponseEntity<Void> notFound() {
    return ResponseEntity.notFound().build();
  }

  // Cuando hay violaciones de integridad (FK, unique, etc.)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of(
            "error", "No se puede eliminar",
            "detalle", "Registros asociados o restricción de integridad"));
  }

  // Opcional: por si en algún lado usan findById(...).get()
  @ExceptionHandler(java.util.NoSuchElementException.class)
  public ResponseEntity<Void> noSuchElement() {
    return ResponseEntity.notFound().build();
  }
}
