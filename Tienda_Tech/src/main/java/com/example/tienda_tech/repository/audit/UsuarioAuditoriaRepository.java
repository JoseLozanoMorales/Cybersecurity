package com.example.tienda_tech.repository.audit;

import com.example.tienda_tech.model.UsuarioAuditoria;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsuarioAuditoriaRepository extends JpaRepository<UsuarioAuditoria, Integer> {

    @Modifying
    @Transactional
    @Query(value = "CALL public.sp_usuario_auditoria_agregar(:p_usuario_id)", nativeQuery = true)
    void auditarLogin(@Param("p_usuario_id") Integer usuarioId);

    // ======== NUEVO: lectura por FUNCIONES (no @Modifying) ========
    @Query(value = "select * from fn_aud_usuarios()", nativeQuery = true)
    List<String> fnUsuarios(); // llena el <select id="au_usuario">

    @Query(value = "select * from fn_aud_logins(:usuario, :desde, :hasta)", nativeQuery = true)
    List<Object[]> fnLoginsRaw(
            @Param("usuario") String usuario,
            @Param("desde")   LocalDate desde,
            @Param("hasta")   LocalDate hasta   // día calendario; la función ya usa < (hasta+1)
    );
}
