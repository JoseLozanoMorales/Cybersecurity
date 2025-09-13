// src/main/java/com/example/tienda_tech/service/GaleriaSpService.java
package com.example.tienda_tech.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.node.BooleanNode;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaleriaSpService {

  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper = new ObjectMapper();

  private static final long MAX_10MB = 10L * 1024 * 1024;
  private static final List<String> ALLOWED = List.of("image/jpeg", "image/png", "image/webp");

  @Transactional
  public int subirDesdeMultipart(
      long productoId,
      List<MultipartFile> files,
      boolean paraGaleria,
      boolean paraMenu,
      boolean marcarComoPortada,
      String descripcionComun
  ) {
    if (files == null || files.isEmpty()) return 0;

    ArrayNode arr = mapper.createArrayNode();
    boolean portadaPuesta = false;

    for (MultipartFile f : files) {
      if (f.isEmpty()) continue;

      String mime = f.getContentType();
      if (mime == null || !ALLOWED.contains(mime)) {
        throw new IllegalArgumentException("Tipo no permitido: " + mime);
      }
      if (f.getSize() > MAX_10MB) {
        throw new IllegalArgumentException("Archivo supera 10MB: " + f.getOriginalFilename());
      }

      String b64;
      try {
        b64 = Base64.getEncoder().encodeToString(f.getBytes());
      } catch (Exception e) {
        throw new RuntimeException("No se pudo leer el archivo: " + f.getOriginalFilename(), e);
      }

      ObjectNode item = arr.addObject();
      item.put("producto_id", (int) productoId);
      item.put("descripcion", (descripcionComun != null && !descripcionComun.isBlank())
          ? descripcionComun
          : f.getOriginalFilename());
      item.put("para_galeria", paraGaleria);
      item.put("para_menu", paraMenu);
      item.put("mime_type", mime);
      item.put("bytes_b64", b64);

      // Solo una portada (si el usuario marcó esa intención)
      boolean esPortada = marcarComoPortada && !portadaPuesta;
      item.put("es_portada", esPortada);
      if (esPortada) portadaPuesta = true;
    }

    String payload = arr.toString();
    // Procedimiento: usa CAST a jsonb
    jdbc.update("CALL public.sp_galeria_agregar_json(?::jsonb)", payload);
    log.info("Galería insertada para producto {} ({} items)", productoId, arr.size());
    return arr.size();
  }

//   @Transactional
//   public int subirDesdeJson(long productoId, List<Map<String, Object>> items) {
//     // Forzamos producto_id desde path
//     ArrayNode arr = mapper.createArrayNode();
//     for (Map<String,Object> raw : items) {
//       ObjectNode n = mapper.convertValue(raw, ObjectNode.class);
//       n.put("producto_id", (int) productoId);
//       arr.add(n);
//     }
//     jdbc.update("CALL public.sp_galeria_agregar_json(?::jsonb)", arr.toString());
//     return arr.size();
//   }
// GaleriaSpService.java (método JSON)
    @Transactional
    public int subirDesdeJson(long productoId, List<Map<String, Object>> items) {
    ArrayNode arr = mapper.createArrayNode();
    for (Map<String,Object> raw : items) {
        ObjectNode n = mapper.convertValue(raw, ObjectNode.class);
        n.put("producto_id", (int) productoId);

        // Normaliza nombres de campo
        if (n.hasNonNull("contenido_base64") && !n.has("bytes_b64")) {
        n.put("bytes_b64", n.get("contenido_base64").asText());
        n.remove("contenido_base64");
        }

        // Defaults sensatos si no vienen
        n.putIfAbsent("para_galeria", BooleanNode.TRUE);
        n.putIfAbsent("para_menu",   BooleanNode.FALSE);
        n.putIfAbsent("es_portada",  BooleanNode.FALSE);

        arr.add(n);
    }
    jdbc.update("CALL public.sp_galeria_agregar_json(?::jsonb)", arr.toString());
    return arr.size();
    }

}
