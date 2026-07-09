// src/main/java/com/example/tienda_tech/repository/ProductoRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Producto; // usa tu entidad Producto
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

  // existsById estándar (ya viene de JpaRepository)

  /*Chequeo nativo directo a la tabla, evita errores de mapeo
  @Query(value = "select count(*) > 0 from producto where producto_id = :id", nativeQuery = true)
  boolean existsByIdNative(@Param("id") Integer id);*/
}
