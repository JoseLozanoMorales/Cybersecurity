package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.AvatarService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
public class AvatarController {

  @Value("${app.upload-root}")
  private String uploadRoot;

  private final AvatarService avatarService;

  public AvatarController(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  private static final long MAX_BYTES = DataSize.ofMegabytes(5).toBytes();
  private static final Set<String> ALLOWED_CT = Set.of("image/png","image/jpeg","image/webp");

  @PostMapping(value="/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadAvatar(@PathVariable Integer id,
                                        @RequestPart("file") MultipartFile file) throws Exception {
    // Validaciones básicas
    if (file.isEmpty()) return bad("Archivo vacío");
    if (file.getSize() > MAX_BYTES) return bad("Archivo supera el límite");
    String ct = Optional.ofNullable(file.getContentType()).orElse("");
    if (!ALLOWED_CT.contains(ct)) return bad("Formato no permitido (usa PNG/JPEG/WebP)");

    BufferedImage img = ImageIO.read(file.getInputStream());
    if (img == null) return bad("El archivo no es una imagen válida");

    // Guardar como PNG
    Path baseDir = Paths.get(uploadRoot, "avatars", id.toString());
    Files.createDirectories(baseDir);
    Path out = baseDir.resolve("avatar.png"); // <-- declarado antes del try/catch

    try (OutputStream os = Files.newOutputStream(out,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ImageIO.write(img, "png", os);
    }

    String publicUrl = "/uploads/avatars/" + id + "/avatar.png";

    try {
      avatarService.actualizarAvatar(id, publicUrl);  // CALL sp_clientes_v2_actualizar_json
    } catch (DataAccessException ex) {
      // rollback archivo si el SP falla
      try { Files.deleteIfExists(out); } catch (IOException ignored) {}
      String msg = Optional.ofNullable(ex.getMostSpecificCause())
                           .map(Throwable::getMessage)
                           .orElse("Error BD al actualizar avatar");
      return bad(msg);
    }

    return ResponseEntity.ok(Map.of("ok", true, "usuario_id", id, "url", publicUrl));
  }

  @DeleteMapping("/{id}/avatar")
  public ResponseEntity<?> removeAvatar(@PathVariable Integer id) {
    try {
      avatarService.removerAvatar(id); // CALL sp_clientes_v2_remover_imagen_json
    } catch (DataAccessException ex) {
      String msg = Optional.ofNullable(ex.getMostSpecificCause())
                           .map(Throwable::getMessage)
                           .orElse("Error BD al remover avatar");
      return bad(msg);
    }

    // Borrado físico best-effort
    Path out = Paths.get(uploadRoot, "avatars", id.toString(), "avatar.png");
    try { Files.deleteIfExists(out); } catch (IOException ignored) {}

    return ResponseEntity.ok(Map.of("ok", true, "usuario_id", id));
  }

  private static ResponseEntity<Map<String, Object>> bad(String msg) {
    return ResponseEntity.badRequest().body(Map.of("ok", false, "error", msg));
  }
}
