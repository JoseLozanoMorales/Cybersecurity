// src/main/java/com/example/tienda_tech/controller/ProductoQueryController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.ProductoCategoriaItemDTO;
import com.example.tienda_tech.dto.ProductoDetalleDTO;
import com.example.tienda_tech.repository.ProductoQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoQueryController {

  private final ProductoQueryRepository repo;

  // (ya existente)
  @GetMapping("/por-categoria")
  public List<ProductoCategoriaItemDTO> porCategoria(@RequestParam Integer categoriaId){
    return repo.listarPorCategoria(categoriaId).stream().map(r ->
      new ProductoCategoriaItemDTO(
        r.getId(), r.getNombre(), r.getPrecio(), r.getMarca(),
        r.getImagen_id(), r.getMime_type()
      )
    ).toList();
  }

  // ✅ NUEVO: Detalle por id
  @GetMapping("/{id}")
  public ProductoDetalleDTO detalle(@PathVariable Integer id){
    var base = repo.detalle(id).stream().findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

    var gal = repo.galeria(id).stream().map(g ->
      new ProductoDetalleDTO.GaleriaItemDTO(
        g.getGaleria_id(), g.getMime_type(),
        Boolean.TRUE.equals(g.getEs_portada()),
        g.getPosicion_galeria()
      )
    ).toList();

    return new ProductoDetalleDTO(
      base.getId(), base.getNombre(), base.getPrecio(),
      base.getMarca(), base.getImagen_id(), base.getMime_type(), gal
    );
  }
}
