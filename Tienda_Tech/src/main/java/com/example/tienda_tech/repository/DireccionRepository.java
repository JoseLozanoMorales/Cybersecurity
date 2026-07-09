package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Short> {

    // Listado solo habilitadas
    List<Direccion> findByUsuario_UsuarioIdAndHabilitadoTrue(Integer usuarioId);

    // === SP wrappers ===
    @Modifying
    @Transactional
    @Query(value = """
      CALL sp_procesar_direcciones(
        jsonb_build_array(
          jsonb_build_object(
            'Accion','agregar',
            'Calle', :calle,
            'Referencia', :referencia,
            'UsuarioId', :usuarioId,
            'CiudadId', :ciudadId
          )
        )
      )
      """, nativeQuery = true)
    void agregar(@Param("usuarioId") Integer usuarioId,
                 @Param("ciudadId")  Short ciudadId,
                 @Param("calle")     String calle,
                 @Param("referencia") String referenciaNullable);

    @Modifying
    @Transactional
    @Query(value = """
      CALL sp_procesar_direcciones(
        jsonb_build_array(
          jsonb_build_object(
            'Accion','editar',
            'DireccionId', :id,
            'Calle', :calle,
            'Referencia', :referencia,
            'UsuarioId', :usuarioId,
            'CiudadId', :ciudadId
          )
        )
      )
      """, nativeQuery = true)
    void editar(@Param("id")       Short direccionId,
                @Param("usuarioId") Integer usuarioId,
                @Param("ciudadId")  Short ciudadId,
                @Param("calle")     String calleNullable,
                @Param("referencia") String referenciaNullable);

    @Modifying
    @Transactional
    @Query(value = """
      CALL sp_procesar_direcciones(
        jsonb_build_array(
          jsonb_build_object(
            'Accion','eliminar',
            'DireccionId', :id
          )
        )
      )
      """, nativeQuery = true)
    void eliminar(@Param("id") Short direccionId);
}
