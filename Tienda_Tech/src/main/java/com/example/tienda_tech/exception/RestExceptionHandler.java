package com.example.tienda_tech.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<?> sql(DataAccessException ex) {
    String msg = ex.getMostSpecificCause() != null
        ? ex.getMostSpecificCause().getMessage()
        : ex.getMessage();

    // Heurística: si el mensaje indica choque de restricciones/“límite”, lo tratamos como 409
    HttpStatus status = (msg != null && msg.toLowerCase().contains("límite"))
        ? HttpStatus.CONFLICT
        : HttpStatus.INTERNAL_SERVER_ERROR;

    return ResponseEntity.status(status).body(Map.of("error", msg));
  }

  // (Opcional) último catch-all
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> generic(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Error interno", "detail", ex.getMessage()));
  }
}
