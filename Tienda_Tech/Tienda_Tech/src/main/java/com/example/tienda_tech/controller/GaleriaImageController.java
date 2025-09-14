package com.example.tienda_tech.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/galeria")
@RequiredArgsConstructor
public class GaleriaImageController {
  private final JdbcTemplate jdbc;

  @GetMapping("/img/{galeriaId}")
  public ResponseEntity<byte[]> getImg(@PathVariable int galeriaId) {
    String sql = "SELECT mime, data FROM public.galeria_img(?)";
    return jdbc.query(sql, ps -> ps.setInt(1, galeriaId), rs -> {
      if (!rs.next()) return ResponseEntity.notFound().build();
      byte[] bytes = rs.getBytes("data");
      if (bytes == null || bytes.length == 0) return ResponseEntity.notFound().build();
      String mime = Optional.ofNullable(rs.getString("mime")).orElse("image/jpeg");
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, mime)
          .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
          .body(bytes);
    });
  }
}


