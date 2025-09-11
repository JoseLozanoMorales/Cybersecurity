// src/main/java/com/example/tienda_tech/repository/CategoriaQueryRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.CategoriaProducto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import java.util.List;

public interface CategoriaQueryRepository extends Repository<CategoriaProducto, Integer> {

  interface Row {
    Integer getId_categoria();
    String  getNombre();
  }

  @Query(value = "select * from fn_listar_categorias()", nativeQuery = true)
  List<Row> listar();
}
