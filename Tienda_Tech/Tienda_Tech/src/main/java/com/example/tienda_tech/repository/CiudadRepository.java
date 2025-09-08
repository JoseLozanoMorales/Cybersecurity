// src/main/java/com/example/tienda_tech/repository/CiudadRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Ciudad;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CiudadRepository extends JpaRepository<Ciudad, Short> {

    @Modifying @Transactional
    @Query(value =
            "CALL sp_procesar_ciudades(" +
                    "  jsonb_build_array(jsonb_build_object('Accion','agregar','Nombre', :nombre,'ProvinciaId', :provinciaId))" +
                    ")", nativeQuery = true)
    void agregar(@Param("nombre") String nombre, @Param("provinciaId") Short provinciaId);

    @Modifying @Transactional
    @Query(value =
            "CALL sp_procesar_ciudades(" +
                    "  jsonb_build_array(jsonb_build_object('Accion','editar','CiudadId', :id,'Nombre', :nombre,'ProvinciaId', :provinciaId))" +
                    ")", nativeQuery = true)
    void editar(@Param("id") Short id,
                @Param("nombre") String nombreNullable,   // puede ir null para que el SP haga COALESCE
                @Param("provinciaId") Short provinciaId); // <-- obligatorio

    @Modifying @Transactional
    @Query(value =
            "CALL sp_procesar_ciudades(" +
                    "  jsonb_build_array(jsonb_build_object('Accion','eliminar','CiudadId', :id))" +
                    ")", nativeQuery = true)
    void eliminar(@Param("id") Short id);
}
