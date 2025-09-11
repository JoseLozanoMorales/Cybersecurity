// src/main/java/com/example/tienda_tech/repository/ProductoQueryRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoQueryRepository extends Repository<Producto, Integer> {

  interface RowCat {
    Integer getId();
    String  getNombre();
    BigDecimal getPrecio();
    String  getMarca();
    Integer getImagen_id();
    String  getMime_type();
  }

  interface RowDet {
    Integer getId();
    String  getNombre();
    BigDecimal getPrecio();
    String  getMarca();
    Integer getImagen_id();
    String  getMime_type();
  }

  interface RowGal {
    Integer getGaleria_id();
    String  getMime_type();
    Boolean getEs_portada();
    Boolean getPara_galeria();
    Integer getPosicion_galeria();
  }

  @Query(value = "select * from fn_buscar_productos_por_categoria(:catId)", nativeQuery = true)
  List<RowCat> listarPorCategoria(@Param("catId") Integer categoriaId);

  @Query(value = "select * from fn_producto_detalle(:id)", nativeQuery = true)
  List<RowDet> detalle(@Param("id") Integer id);

  @Query(value = "select * from fn_producto_galeria(:id)", nativeQuery = true)
  List<RowGal> galeria(@Param("id") Integer id);
}
