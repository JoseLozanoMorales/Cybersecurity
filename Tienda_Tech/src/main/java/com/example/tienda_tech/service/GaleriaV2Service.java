// src/main/java/com/example/tienda_tech/service/GaleriaV2Service.java
package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.GaleriaV2Dtos.GaleriaItemDto;
import com.example.tienda_tech.dto.galeria.GaleriaDTO;
import com.example.tienda_tech.model.GaleriaProductoV2;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GaleriaV2Service {

  private final JdbcTemplate jdbc;

  public List<GaleriaItemDto> listar(int productoId, String scope) {
    return jdbc.query(
      "SELECT galeria_id, descripcion, es_portada, para_galeria, para_menu, " +
      "posicion_galeria, posicion_menu, mime_type, peso_bytes, ancho, alto, habilitado " +
      "FROM public.fn_galeria_v2_listar(?, ?)",
      (rs, i) -> {
        GaleriaItemDto d = new GaleriaItemDto();
        d.setGaleriaId(rs.getInt("galeria_id"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setEsPortada(rs.getBoolean("es_portada"));
        d.setParaGaleria(rs.getBoolean("para_galeria"));
        d.setParaMenu(rs.getBoolean("para_menu"));
        d.setPosicionGaleria((Integer) rs.getObject("posicion_galeria"));
        d.setPosicionMenu((Integer) rs.getObject("posicion_menu"));
        d.setMimeType(rs.getString("mime_type"));
        d.setPesoBytes((Long) rs.getObject("peso_bytes"));
        d.setAncho((Integer) rs.getObject("ancho"));
        d.setAlto((Integer) rs.getObject("alto"));
        d.setHabilitado(rs.getBoolean("habilitado"));
        return d;
      },
      productoId, scope
    );
  }

  public record MediaDto(byte[] bytes, String mimeType, Long length) {}
  public MediaDto obtenerMedia(int galeriaId) {
    return jdbc.queryForObject(
      "SELECT contenido, mime_type, peso_bytes FROM public.galeria_productos_v2 WHERE galeria_id=?",
      (rs, i) -> new MediaDto(rs.getBytes("contenido"),
                              rs.getString("mime_type"),
                              (Long) rs.getObject("peso_bytes")),
      galeriaId
    );
  }

    public int agregar(Integer productoId, MultipartFile file, String descripcion,
                       Boolean esPortada, Boolean paraGaleria, Boolean paraMenu,
                       Integer posGaleria, Integer posMenu, Integer ancho, Integer alto) {
        try {
            byte[] bytes = file.getBytes();
            String mime = file.getContentType();
            return jdbc.queryForObject(
                    "SELECT public.sp_galeria_v2_agregar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Integer.class,
                    productoId, bytes, mime, descripcion,
                    esPortada, paraGaleria, paraMenu,
                    posGaleria, posMenu, ancho, alto
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen", e);
        }
    }


  public void setPortada(int productoId, int galeriaId) {
    jdbc.update("CALL public.sp_galeria_v2_set_portada(?, ?)", productoId, galeriaId);
  }

  public void reordenar(int productoId, String scope, List<Integer> ids) {
    jdbc.execute((Connection con) -> {
      try (PreparedStatement ps = con.prepareStatement("CALL public.sp_galeria_v2_reordenar(?, ?, ?)")) {
        ps.setInt(1, productoId);
        ps.setString(2, scope);
        Array arr = con.createArrayOf("int4", ids.toArray());
        ps.setArray(3, arr);
        ps.execute();
      }
      return null;
    });
  }

  public void eliminar(int galeriaId) {
    jdbc.update("CALL public.sp_galeria_v2_eliminar(?)", galeriaId);
  }
  // GaleriaV2Service.java
    public void actualizarFlags(int galeriaId, Boolean habilitado, Boolean paraGaleria, Boolean paraMenu) {
    jdbc.update("""
        UPDATE public.galeria_productos_v2
            SET habilitado   = COALESCE(?, habilitado),
                para_galeria = COALESCE(?, para_galeria),
                para_menu    = COALESCE(?, para_menu)
        WHERE galeria_id = ?
        """,
        habilitado, paraGaleria, paraMenu, galeriaId
    );
    }
    // GaleriaV2Service.java
    public int agregarBytes(int productoId,
                            byte[] bytes,
                            String mimeType,
                            String descripcion,
                            Boolean esPortada,
                            Boolean paraGaleria,
                            Boolean paraMenu,
                            Integer posGaleria,
                            Integer posMenu,
                            Integer ancho,
                            Integer alto) {

        return jdbc.queryForObject(
                "SELECT public.sp_galeria_v2_agregar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Integer.class,
                productoId, bytes, mimeType, descripcion,
                esPortada, paraGaleria, paraMenu,
                posGaleria, posMenu, ancho, alto
        );
    }


  // PATCH flags + descripcion (batch)
    public void actualizarFlagsYDescripcion(int galeriaId,
                                            Boolean habilitado,
                                            Boolean paraGaleria,
                                            Boolean paraMenu,
                                            String descripcion) {
        jdbc.update("""
            UPDATE public.galeria_productos_v2
            SET habilitado   = COALESCE(?, habilitado),
                para_galeria = COALESCE(?, para_galeria),
                para_menu    = COALESCE(?, para_menu),
                descripcion  = COALESCE(?, descripcion)
            WHERE galeria_id = ?
            """,
            habilitado, paraGaleria, paraMenu, descripcion, galeriaId
        );

    }

}
