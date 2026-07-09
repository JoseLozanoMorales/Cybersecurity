package com.example.tienda_tech.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SuggestionRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om = new ObjectMapper();

    public void createV3(Map<String,Object> payload, Integer topN, Integer encuestaId, String userFallback) throws Exception {
        String json = om.writeValueAsString(payload);
        jdbc.update("CALL public.sp_sugerencia_crear_v3_json(?::jsonb, ?::varchar, ?::int, ?::smallint)",
                json, userFallback, topN==null?3:topN, encuestaId==null?1:encuestaId);
    }

    public Map<String,Object> getResult(Integer sugerenciaId) throws Exception {
        String json = jdbc.queryForObject("SELECT public.f_sugerencia_resultado(?::int,true)", String.class, sugerenciaId);
        return om.readValue(json, new TypeReference<>() {});
    }

    public Map<String,Object> getLastForUser(Integer usuarioId) throws Exception {
        String json = jdbc.queryForObject("SELECT public.f_sugerencia_resultado_ultima(?::int,true)", String.class, usuarioId);
        return om.readValue(json, new TypeReference<>() {});
    }

    public void updateV3(Integer sugId, Map<String,Object> payload, Integer topN, Integer encuestaId, Boolean merge) throws Exception {
        String json = om.writeValueAsString(payload);
        jdbc.update("CALL public.sp_sugerencia_actualizar_v3_json(?::int, ?::jsonb, ?::int, ?::smallint, ?::boolean)",
                sugId, json, topN==null?3:topN, encuestaId==null?1:encuestaId, merge==null?false:merge);
    }

    public void toggle(Integer sugId, Boolean disable) {
        jdbc.update("CALL public.sp_sugerencia_deshabilitar(?::int, ?::boolean)", sugId, disable==null?true:disable);
    }

    public void deleteIfDisabled(Integer sugId) {
        jdbc.update("CALL public.sp_sugerencia_eliminar_si_deshabilitada(?::int)", sugId);
    }
    public Map<String,Object> listForUser(Integer usuarioId, Integer limit, Integer offset) throws Exception {
        String json = jdbc.queryForObject(
                "SELECT public.f_sugerencia_historial(?::int, ?::int, ?::int)", String.class,
                usuarioId, limit==null?20:limit, offset==null?0:offset
        );
        return new ObjectMapper().readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>(){});
    }
    public Map<String,Object> createAndReturn(Map<String,Object> respuestas,
                                              Integer topN,
                                              Integer encuestaId,
                                              String usuarioFallback) throws Exception {
        String json = om.writeValueAsString(respuestas);

        // 1) Crea la sugerencia y devuelve el ID (nota el ::smallint)
        Integer sugId = jdbc.queryForObject(
                "SELECT public.api_sugerencia_crear(?::jsonb, ?::varchar, ?::int, ?::smallint)",
                Integer.class,
                json, usuarioFallback, topN, encuestaId.shortValue()
        );

        // 2) Devuelve el resultado listo para el modal
        String resultJson = jdbc.queryForObject(
                "SELECT public.f_sugerencia_resultado(?::int, true)",
                String.class,
                sugId
        );

        return new ObjectMapper().readValue(resultJson, new com.fasterxml.jackson.core.type.TypeReference<>(){});
    }
}
