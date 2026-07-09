package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Producto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AlmacenamientoSpRepository extends JpaRepository<Producto, Integer> {

    @Transactional
    @Modifying
    @Query(value = "CALL public.agregar_almacenamiento(:p_nombre, :p_preciounitario, :p_enlace, :p_stock, :p_marca_id, :p_gama_id, :p_iva_id, :p_costo, :p_capacidad, :p_tipo)", 
           nativeQuery = true)
    void agregarAlmacenamiento(
            @Param("p_nombre") String nombre,
            @Param("p_preciounitario") java.math.BigDecimal preciounitario,
            @Param("p_enlace") String enlace,
            @Param("p_stock") Integer stock,
            @Param("p_marca_id") Integer marcaId,
            @Param("p_gama_id") Integer gamaId,
            @Param("p_iva_id") Integer ivaId,
            @Param("p_costo") java.math.BigDecimal costo,
            @Param("p_capacidad") Long capacidad,
            @Param("p_tipo") String tipo
    );
}
