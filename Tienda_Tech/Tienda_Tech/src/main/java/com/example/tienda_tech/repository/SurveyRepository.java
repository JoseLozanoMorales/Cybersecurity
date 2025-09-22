package com.example.tienda_tech.repository;

import com.example.tienda_tech.dto.encuesta.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SurveyRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om = new ObjectMapper();

    public Map<String,Object> getSurveyJson() throws Exception {
        String json = jdbc.queryForObject("SELECT public.f_encuesta_json()", String.class);
        return om.readValue(json, new TypeReference<>() {});
    }

    public void updateEncuesta(EncuestaUpdate u) {
        if (u.nombre() != null)
            jdbc.update("UPDATE public.encuesta SET nombre=? WHERE encuesta_id=1", u.nombre());
        if (u.habilitado() != null)
            jdbc.update("UPDATE public.encuesta SET habilitado=? WHERE encuesta_id=1", u.habilitado());
    }

    public void upsertPregunta(PreguntaUpsert p) {
        jdbc.update("CALL public.sp_encuesta_pregunta_upsert(?::smallint, ?::varchar, ?::text, ?::int, ?::boolean)",
                1, p.key(), p.texto(), p.orden(), p.habilitado());
    }
    public void deletePregunta(String key) {
        jdbc.update("CALL public.sp_encuesta_pregunta_delete(?::smallint, ?::varchar)", 1, key);
    }

    public void upsertOpcion(OpcionUpsert o) {
        jdbc.update("CALL public.sp_encuesta_opcion_upsert(?::smallint, ?::varchar, ?::varchar, ?::text, ?::int, ?::boolean)",
                1, o.key(), o.valor(), o.texto(), o.orden(), o.habilitado());
    }
    public void deleteOpcion(String key, String valor) {
        jdbc.update("CALL public.sp_encuesta_opcion_delete(?::smallint, ?::varchar, ?::varchar)", 1, key, valor);
    }

    public void upsertRegla(ReglaUpsert r) {
        jdbc.update("CALL public.sp_encuesta_regla_upsert(?::smallint, ?::varchar, ?::varchar, ?::varchar, ?::varchar, ?::smallint, ?::boolean)",
                1, r.key(), r.valor(), r.categoria(), r.gamaObjetivo(), r.deltaRank(), r.habilitado());
    }
    public void deleteRegla(String key, String valor, String categoria) {
        jdbc.update("CALL public.sp_encuesta_regla_delete(?::smallint, ?::varchar, ?::varchar, ?::varchar)",
                1, key, valor, categoria);
    }
}
