// src/main/java/com/example/tienda_tech/repository/UsuarioRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByUsuario(String usuario);

    // Ya existente (registro público)
    @Modifying
    @Transactional
    @Query(
    value = "CALL public.registrar_usuario_cliente(:p_nombre, :p_cedula, :p_correo, :p_telefono, :p_usuario, :p_contrasenia)",
    nativeQuery = true
    )
    void registrarClienteSP(
    @Param("p_nombre")   String nombre,
    @Param("p_cedula")   String cedula,
    @Param("p_correo")   String correo,
    @Param("p_telefono") String telefono,
    @Param("p_usuario")  String usuario,
    @Param("p_contrasenia") String contraseniaHash   // <<--- HASH
    );


    @Modifying @Transactional
    @Query(value = "CALL crear_usuario(:v_nombre, :v_cedula, :v_correo, :v_telefono, :v_contrasenia, :v_usuario, :v_id_metodopago, :v_id_rol)", nativeQuery = true)
    void crearUsuarioSP(
            @Param("v_nombre") String nombre,
            @Param("v_cedula") String cedula,
            @Param("v_correo") String correo,
            @Param("v_telefono") String telefono,
            @Param("v_contrasenia") String contrasenia,  // OJO: sin ñ
            @Param("v_usuario") String usuario,
            @Param("v_id_metodopago") Integer idMetodoPago, // puede ser null
            @Param("v_id_rol") Integer idRol
    );
    @Modifying
    @Transactional
    @Query(value =
        "CALL public.actualizar_usuario_cliente(" +
        ":p_usuario_id, :p_nombre, :p_cedula, :p_correo, :p_telefono, :p_usuario, :p_contrasenia)", 
        nativeQuery = true)
    void actualizarClienteSP(
        @Param("p_usuario_id") Integer usuarioId,
        @Param("p_nombre") String nombre,
        @Param("p_cedula") String cedula,
        @Param("p_correo") String correo,
        @Param("p_telefono") String telefono,
        @Param("p_usuario") String usuario,
        @Param("p_contrasenia") String contrasenia
    );

    // ====== NUEVO (solo admins/trabajadores) ======

    // Si en tu BD es PROCEDURE:
    @Modifying
    @jakarta.transaction.Transactional
    @Query(value = "CALL public.gestionar_admins_json(CAST(?1 AS jsonb))", nativeQuery = true)
    void gestionarAdminsJsonCall(String payloadJson);

    // Si en tu BD es FUNCTION:
    @Modifying(clearAutomatically = true)
    @jakarta.transaction.Transactional
    @Query(value = "SELECT public.gestionar_admins_json(CAST(?1 AS jsonb))", nativeQuery = true)
    void gestionarAdminsJsonSelect(String payloadJson);

    @Modifying
    @Transactional
    @Query(value =
            "CALL public.sp_actualizar_contrasenias(" +
                    "  jsonb_build_array(" +
                    "    jsonb_build_object('Correo', lower(:correo), 'Contrasenia', :hash)" +
                    "  )" +
                    ")", nativeQuery = true)
    void actualizarContraseniaPorCorreoCall(@Param("correo") String correo,
                                            @Param("hash") String hash);
    List<Usuario> findTop50ByUsuarioContainingIgnoreCase(String usuario);
    List<Usuario> findTop50ByUsuarioContainingIgnoreCaseAndIdRol(String usuario, Integer idRol);
}
