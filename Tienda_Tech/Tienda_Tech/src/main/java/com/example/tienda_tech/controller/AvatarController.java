package com.example.tienda_tech.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
public class AvatarController {

  @Value("${app.upload-root}")
  private String uploadRoot;

  private final JdbcTemplate jdbc;

  public AvatarController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final long MAX_BYTES = DataSize.ofMegabytes(5).toBytes();
  private static final Set<String> ALLOWED_CT = Set.of("image/png","image/jpeg","image/webp");

  @PostMapping(value="/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadAvatar(@PathVariable Integer id,
                                        @RequestPart("file") MultipartFile file) throws Exception {
    if (file.isEmpty()) return bad("Archivo vacío");
    if (file.getSize() > MAX_BYTES) return bad("Archivo supera el límite");

    // Validar por content-type anunciado
    String ct = Optional.ofNullable(file.getContentType()).orElse("");
    if (!ALLOWED_CT.contains(ct)) return bad("Formato no permitido (usa PNG/JPEG/WebP)");

    // Validar que realmente sea imagen
    BufferedImage img = ImageIO.read(file.getInputStream());
    if (img == null) return bad("El archivo no es una imagen válida");

    // Normalizamos a PNG (eliminas EXIF y evitas problemas)
    Path baseDir = Paths.get(uploadRoot, "avatars", id.toString());
    Files.createDirectories(baseDir);

    // Nombre simple (si cambias el archivo, el navegador puede cachearlo; si pasa, añade ?t=timestamp)
    Path out = baseDir.resolve("avatar.png");
    try (OutputStream os = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ImageIO.write(img, "png", os);
    }

    String publicUrl = "/uploads/avatars/" + id + "/avatar.png";

    int updated = jdbc.update(
      "UPDATE public.usuario SET avatar_path = ? WHERE usuario_id = ?",
      publicUrl, id
    );
    if (updated == 0) return bad("Usuario no encontrado");

    Map<String, Object> body = Map.of(
      "ok", true,
      "usuario_id", id,
      "url", publicUrl
    );
    return ResponseEntity.ok(body);
  }

  @DeleteMapping("/{id}/avatar")
  public ResponseEntity<?> removeAvatar(@PathVariable Integer id) {
    // Poner NULL en BD (opcionalmente borrar archivo)
    jdbc.update("UPDATE public.usuario SET avatar_path = NULL WHERE usuario_id = ?", id);

    Path out = Paths.get(uploadRoot, "avatars", id.toString(), "avatar.png");
    try { Files.deleteIfExists(out); } catch (IOException ignored) {}

    return ResponseEntity.ok(Map.of("ok", true, "usuario_id", id));
  }

  private static ResponseEntity<Map<String, Object>> bad(String msg) {
    return ResponseEntity.badRequest().body(Map.of("ok", false, "error", msg));
  }
}
