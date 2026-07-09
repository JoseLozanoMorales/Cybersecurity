package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.MetodoPago;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    // Lectura: función fn_metodopago_por_usuario(p_usuario_id)
    @Query(value = """
        SELECT
          metodopago_id,
          numero_mascara,
          fecha_expiracion,
          habilitado,
          tipo_id,
          tipo_nombre
        FROM fn_metodopago_por_usuario(:userId)
        """, nativeQuery = true)
    List<Object[]> fnListarPorUsuario(@Param("userId") Integer userId);

    // Catálogo: función fn_tipos_metodopago()
    @Query(value = """
        SELECT
          tipo_id,
          nombre
        FROM fn_tipos_metodopago()
        """, nativeQuery = true)
    List<Object[]> fnListarTipos();

    // ===== Escrituras con JSONB construido en SQL =====

    // AGREGAR
    @Modifying
    @Query(value = """
      CALL sp_procesar_metodopago(
        jsonb_build_array(
          jsonb_strip_nulls(
            jsonb_build_object(
              'Accion',    'agregar',
              'NumeroTar', :numero,
              'FechaEx',   CAST(:fecha AS date),
              'TipoId',    :tipoId,
              'UsuarioId', :usuarioId
            )
          )
        )
      )
      """, nativeQuery = true)
    void agregar(@Param("numero") String numero,
                 @Param("fecha") LocalDate fecha,     // p.ej. 2028-12-31
                 @Param("tipoId") Integer tipoId,
                 @Param("usuarioId") Integer usuarioId);

    // ELIMINAR
    @Modifying
    @Query(value = """
      CALL sp_procesar_metodopago(
        jsonb_build_array(
          jsonb_build_object('Accion','eliminar','MetodoId', :metodoId)
        )
      )
      """, nativeQuery = true)
    void eliminar(@Param("metodoId") Integer metodoId);
}
