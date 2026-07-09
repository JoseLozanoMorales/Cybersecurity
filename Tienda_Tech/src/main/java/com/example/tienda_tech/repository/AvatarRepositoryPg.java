package com.example.tienda_tech.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class AvatarRepositoryPg implements AvatarRepository {

  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public AvatarRepositoryPg(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  @Override
  public void actualizarAvatar(int usuarioId, String avatarPath) {
    String payload = toJson(Map.of(
        "usuario_id", usuarioId,
        "avatar_path", avatarPath
    ));
    jdbc.update("CALL public.sp_clientes_v2_actualizar_json(?::jsonb)", payload);
  }

  @Override
  public void removerAvatar(int usuarioId) {
    String payload = toJson(Map.of("usuario_id", usuarioId));
    jdbc.update("CALL public.sp_clientes_v2_remover_imagen_json(?::jsonb)", payload);
  }

  private String toJson(Object obj) {
    try { return mapper.writeValueAsString(obj); }
    catch (Exception e) { throw new IllegalArgumentException("No se pudo serializar payload JSON", e); }
  }
}
