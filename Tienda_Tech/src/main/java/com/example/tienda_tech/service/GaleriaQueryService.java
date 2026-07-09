// src/main/java/com/example/tienda_tech/service/GaleriaQueryService.java
package com.example.tienda_tech.service;

import lombok.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Service
@RequiredArgsConstructor
public class GaleriaQueryService {

  private final JdbcTemplate jdbc;

  // DTO de salida: se serializa a snake_case para el front
  @Getter @Setter @AllArgsConstructor @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class GaleriaItemDto {
    private Integer galeriaId;
    private Integer productoId;
    private String  descripcion;
    private Boolean habilitado;
    private Boolean esPortada;
    private Boolean paraGaleria;
    private Boolean paraMenu;
    private Integer posicionGaleria;
    private Integer posicionMenu;
    private String  mimeType;
    private Long    pesoBytes;
    private Integer ancho;
    private Integer alto;
  }

  public static record MediaPayload(byte[] bytes, String mime) {}

  private static final RowMapper<GaleriaItemDto> MAP = (rs, i) -> {
    GaleriaItemDto d = new GaleriaItemDto();
    d.setGaleriaId(rs.getInt("galeria_id"));
    d.setProductoId(rs.getInt("producto_id"));
    d.setDescripcion(rs.getString("descripcion"));
    d.setHabilitado((Boolean) rs.getObject("habilitado"));
    d.setEsPortada((Boolean) rs.getObject("es_portada"));
    d.setParaGaleria((Boolean) rs.getObject("para_galeria"));
    d.setParaMenu((Boolean) rs.getObject("para_menu"));
    d.setPosicionGaleria((Integer) rs.getObject("posicion_galeria"));
    d.setPosicionMenu((Integer) rs.getObject("posicion_menu"));
    d.setMimeType(rs.getString("mime_type"));
    d.setPesoBytes((Long) rs.getObject("peso_bytes"));
    d.setAncho((Integer) rs.getObject("ancho"));
    d.setAlto((Integer) rs.getObject("alto"));
    return d;
  };

  public List<GaleriaItemDto> listar(long productoId, String vista) {
    String sql   = "SELECT * FROM public.fn_galeria_listar(?, ?)";
    Object[] a   = { (int) productoId, vista };
    int[]    t   = { Types.INTEGER,    Types.VARCHAR };
    return jdbc.query(sql, a, t, MAP);
  }

  public MediaPayload obtenerMedia(int galeriaId){
    return jdbc.<MediaPayload>query(
        "SELECT * FROM public.fn_galeria_media(?)",
        (ResultSetExtractor<MediaPayload>) rs -> {
          if (rs.next()) {
            return new MediaPayload(rs.getBytes("bytes"), rs.getString("mime"));
          }
          return null;
        },
        galeriaId
    );
  }
}
