// src/main/java/com/example/tienda_tech/controller/ProductoQueryController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.ProductoCategoriaItemDTO;
import com.example.tienda_tech.dto.ProductoDetalleDTO;
import com.example.tienda_tech.repository.ProductoQueryRepository;
import com.example.tienda_tech.service.SiemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoQueryController {

  private final ProductoQueryRepository repo;
  private final SiemAuditService siemAuditService;

  /** Listado de productos por categoría */
  @GetMapping("/por-categoria")
  public List<ProductoCategoriaItemDTO> porCategoria(
          @RequestParam Integer categoriaId,
          @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

    String usuario = userId != null ? String.valueOf(userId) : "Anónimo";

    List<ProductoCategoriaItemDTO> lista = repo.listarPorCategoria(categoriaId).stream().map(r ->
            new ProductoCategoriaItemDTO(
                    r.getId(), r.getNombre(), r.getPrecio(), r.getMarca(),
                    r.getImagen_id(), r.getMime_type()
            )
    ).toList();

    siemAuditService.registrarEvento(
            "CATALOGO_CATEGORIA",
            usuario,
            "Productos",
            "Exitoso",
            "Navegó al catálogo de la categoría ID=" + categoriaId + " — " + lista.size() + " producto(s) cargados.",
            "INFO"
    );

    return lista;
  }

  /** Vista de detalle de un producto (página de información del producto) */
  @GetMapping("/{id}")
  public ProductoDetalleDTO detalle(
          @PathVariable Integer id,
          @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

    String usuario = userId != null ? String.valueOf(userId) : "Anónimo";

    var base = repo.detalle(id).stream().findFirst()
            .orElseThrow(() -> {
              siemAuditService.registrarEvento(
                      "PRODUCTO_VER",
                      usuario,
                      "Productos",
                      "No encontrado",
                      "Intentó ver el detalle del producto ID=" + id + " pero no existe.",
                      "ADVERTENCIA"
              );
              return new IllegalArgumentException("Producto no encontrado");
            });

    var gal = repo.galeria(id).stream().map(g ->
            new ProductoDetalleDTO.GaleriaItemDTO(
                    g.getGaleria_id(), g.getMime_type(),
                    Boolean.TRUE.equals(g.getEs_portada()),
                    g.getPosicion_galeria()
            )
    ).toList();

    siemAuditService.registrarEvento(
            "PRODUCTO_VER",
            usuario,
            "Productos",
            "Exitoso",
            "Visualizó la página de detalle del producto \"" + base.getNombre() + "\" (ID=" + id + ").",
            "INFO"
    );

    return new ProductoDetalleDTO(
            base.getId(), base.getNombre(), base.getPrecio(),
            base.getMarca(), base.getImagen_id(), base.getMime_type(), gal
    );
  }
}
