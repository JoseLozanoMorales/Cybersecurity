package com.example.tienda_tech.repository.audit;

import com.example.tienda_tech.model.ProductoAuditoria;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductoAuditoriaRepository extends JpaRepository<ProductoAuditoria, Long> {

    @Query(value = "select * from fn_auditoria_productos(cast(:filtro as jsonb))", nativeQuery = true)
    List<ProductoAuditoria> buscarPorJson(@Param("filtro") String filtroJson);

    @Query(value = "select usuario from fn_auditoria_usuarios()", nativeQuery = true)
    List<String> usuariosUnicos();

}
