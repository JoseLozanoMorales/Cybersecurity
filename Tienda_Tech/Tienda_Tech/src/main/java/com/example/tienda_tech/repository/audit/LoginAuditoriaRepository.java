package com.example.tienda_tech.repository.audit;

import com.example.tienda_tech.model.UsuarioAuditoria;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoginAuditoriaRepository extends JpaRepository<UsuarioAuditoria, Integer> {

    @Query(value = "select * from public.fn_login_auditoria(cast(:filtro as jsonb))",
            nativeQuery = true)
    List<Object[]> buscarLoginsPorJson(@Param("filtro") String filtroJson);

    @Query(value = "select usuario from public.fn_login_auditoria_usuarios(cast(:filtro as jsonb))",
            nativeQuery = true)
    List<String> usuariosAuditoriaPorJson(@Param("filtro") String filtroJson);
}
