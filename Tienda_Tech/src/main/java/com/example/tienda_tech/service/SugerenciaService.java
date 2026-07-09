package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.SqlParameterValue;
import java.sql.Types;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SugerenciaService {

    private final NamedParameterJdbcTemplate jdbc;

    /** Genera la sugerencia llamando al SP. payloadJson debe ser JSON válido (objeto o array). */
    public int generarSugerencia(String payloadJson, String usuario, int topN, int encuestaId) {
        // 1) SIN ::jsonb
        String call = "CALL public.sp_sugerencia_crear_v3_json(:items, :usr, :topn, :eid)";

        MapSqlParameterSource p = new MapSqlParameterSource()
                // 2) JSON como Types.OTHER (PostgreSQL lo mapea a json/jsonb)
                .addValue("items", new SqlParameterValue(Types.OTHER, payloadJson))
                .addValue("usr", usuario)
                .addValue("topn", topN)
                // 3) smallint como short
                .addValue("eid", (short) encuestaId);

        jdbc.update(call, p);

        String q = """
        SELECT s.sugerencia_id
        FROM public.sugerencia s
        JOIN public.usuario u ON u.usuario_id = s.usuario_id
        WHERE u.usuario = :usr
        ORDER BY s.creado_en DESC
        LIMIT 1
    """;
        return jdbc.queryForObject(q, new MapSqlParameterSource("usr", usuario), Integer.class);
    }


    /** Detalle de productos de una sugerencia. */
    public List<Map<String, Object>> detalle(int sugerenciaId) {
        String q = """
        SELECT sp.sugerencia_id,
               sp.producto_id,
               p.nombre,
               p.preciounitario,
               p.categoria_id AS id_categoria,
               c.nombre       AS categoria,
               g.tipo_gama    AS gama,
               COALESCE(sp.score,0) AS score
        FROM public.sugerencia_producto sp
        JOIN public.producto p           ON p.producto_id = sp.producto_id
        JOIN public.categoria_producto c ON c.id_categoria = p.categoria_id
        LEFT JOIN public.gama g          ON g.gama_id = p.gama_id
        WHERE sp.sugerencia_id = :sid
        ORDER BY categoria, score DESC, preciounitario
    """;
        return jdbc.queryForList(q, new MapSqlParameterSource("sid", sugerenciaId));
    }


    /** (Opcional) limpieza de sugerencias previas por usuarioId+encuestaId */
    public void limpiarPrevias(int usuarioId, int encuestaId){
        String sql = """
            DELETE FROM public.respuesta
             WHERE sugerencia_id IN (SELECT sugerencia_id FROM public.sugerencia WHERE usuario_id=:uid AND encuesta_id=:eid);
            DELETE FROM public.sugerencia_producto
             WHERE sugerencia_id IN (SELECT sugerencia_id FROM public.sugerencia WHERE usuario_id=:uid AND encuesta_id=:eid);
            DELETE FROM public.sugerencia
             WHERE usuario_id=:uid AND encuesta_id=:eid;
        """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("uid", usuarioId)
                .addValue("eid", encuestaId);
        jdbc.update(sql, p);
    }
}
