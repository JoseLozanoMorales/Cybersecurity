package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Galeria;
import com.example.tienda_tech.dto.galeria.GaleriaItemDto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GaleriaRepository extends JpaRepository<Galeria, Integer> {

  @Query("""
    select new com.example.tienda_tech.dto.galeria.GaleriaItemDto(
      g.galeriaId, g.esPortada, g.paraGaleria, g.paraMenu,
      g.posicionGaleria, g.posicionMenu, g.habilitado, g.descripcion,
      g.mimeType, g.ancho, g.alto, g.pesoBytes
    )
    from Galeria g
    where g.productoId = :productoId
    order by coalesce(g.posicionGaleria, 999999), g.galeriaId
  """)
  List<GaleriaItemDto> listar(@Param("productoId") long productoId);

  @Modifying
  @Query("update Galeria g set g.posicionGaleria=:pos where g.galeriaId=:id")
  void updatePosGaleria(@Param("id") Integer galeriaId, @Param("pos") Integer pos);

  @Modifying
  @Query("update Galeria g set g.posicionMenu=:pos where g.galeriaId=:id")
  void updatePosMenu(@Param("id") Integer galeriaId, @Param("pos") Integer pos);

  @Modifying
  @Query("update Galeria g set g.esPortada=true where g.productoId=:productoId and g.galeriaId=:galeriaId")
  void marcarPortada(@Param("productoId") long productoId, @Param("galeriaId") Integer galeriaId);
}
