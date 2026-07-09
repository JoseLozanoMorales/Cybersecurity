package com.example.tienda_tech.repository;

import com.example.tienda_tech.dto.MetodoPagoResponse;
import com.example.tienda_tech.util.MaskUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Repository
public class MetodoPagoSpRepository {

    private final JdbcTemplate jdbc;

    public MetodoPagoSpRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ==== SP Calls ====

    public void callAgregar(String numeroTarjeta, String fechaExp, Integer tipoId, Integer usuarioId) {
        // sp_agregar_metodo_pago(character varying, date, integer, integer)
        jdbc.update(con -> {
            var call = con.prepareCall("CALL sp_agregar_metodo_pago(?, ?, ?, ?)");
            call.setString(1, numeroTarjeta);
            call.setDate(2, java.sql.Date.valueOf(fechaExp));
            call.setInt(3, tipoId);
            call.setInt(4, usuarioId);
            return call;
        });
    }

    public void callActualizar(Integer metodopagoId, Integer usuarioId,
                               String numeroTarjeta, String fechaExp, Integer tipoId, Boolean habilitado) {
        // sp_metodopago_actualizar(integer, integer, character varying, date, integer, boolean)
        jdbc.update(con -> {
            var call = con.prepareCall("CALL sp_metodopago_actualizar(?, ?, ?, ?, ?, ?)");
            call.setInt(1, metodopagoId);
            call.setInt(2, usuarioId);
            if (numeroTarjeta != null && !numeroTarjeta.trim().isEmpty()) call.setString(3, numeroTarjeta);
            else call.setNull(3, java.sql.Types.VARCHAR);

            if (fechaExp != null) call.setDate(4, java.sql.Date.valueOf(fechaExp));
            else call.setNull(4, java.sql.Types.DATE);

            if (tipoId != null) call.setInt(5, tipoId);
            else call.setNull(5, java.sql.Types.INTEGER);

            if (habilitado != null) call.setBoolean(6, habilitado);
            else call.setNull(6, java.sql.Types.BOOLEAN);

            return call;
        });
    }

    // ==== Queries auxiliares ====
///ESTO NO VALE, NO DEBE SER USADO

    private static final DateTimeFormatter VENCE_FMT = DateTimeFormatter.ofPattern("MM/yyyy");

    public List<MetodoPagoResponse> listarPorUsuario(Integer usuarioId) {
        String sql = """
            SELECT m.metodopago_id, m.numero_tarjeta, m.fecha_expiracion, m.tipo_id, m.habilitado,
                   (SELECT u.metodopago_id FROM usuario u WHERE u.usuario_id = m.usuario_id) AS preferido_id
              FROM metodopago m
             WHERE m.usuario_id = ?
             ORDER BY m.metodopago_id DESC
            """;

        return jdbc.query(sql, (rs, rowNum) -> mapRow(rs, usuarioId), usuarioId);
    }

    public int actualizarPreferido(Integer usuarioId, Integer metodopagoId) {
        String sql = "UPDATE usuario SET metodopago_id = ? WHERE usuario_id = ?";
        return jdbc.update(sql, metodopagoId, usuarioId);
    }

    private MetodoPagoResponse mapRow(ResultSet rs, Integer usuarioId) throws SQLException {
        var id = rs.getInt("metodopago_id");
        var pan = rs.getString("numero_tarjeta");
        var vence = rs.getDate("fecha_expiracion");
        var tipoId = rs.getInt("tipo_id");
        var habilitado = rs.getBoolean("habilitado");
        var preferidoId = rs.getInt("preferido_id");
        boolean esPref = (preferidoId == id);

        return MetodoPagoResponse.builder()
                .id(id)
                .mascara(MaskUtils.maskLast4(pan))
                .vence(vence != null ? vence.toLocalDate().format(VENCE_FMT) : null)
                .tipoId(tipoId)
                .habilitado(habilitado)
                .esPreferido(esPref)
                .build();
    }
}
