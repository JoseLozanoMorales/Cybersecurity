package com.example.tienda_tech.repository.audit;

import com.example.tienda_tech.model.MovimientoInventarioAuditoria;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioAuditoriaRepository extends JpaRepository<MovimientoInventarioAuditoria, Long> {

    @Query(value = "select * from fn_auditoria_movimientos(cast(:filtro as jsonb))", nativeQuery = true)
    List<MovimientoInventarioAuditoria> buscarPorJson(@Param("filtro") String filtroJson);

    @Query(value = "select usuario from fn_auditoria_usuarios()", nativeQuery = true)
    List<String> usuariosUnicos();
}
