// src/main/java/com/example/tienda_tech/dto/ProductoDetalleDTO.java
package com.example.tienda_tech.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoDetalleDTO(
  Integer id,
  String nombre,
  BigDecimal precio,
  String marca,
  Integer imagenId,
  String mimeType,
  List<GaleriaItemDTO> galeria
){
  public record GaleriaItemDTO(Integer id, String mimeType, boolean portada, Integer pos){}
}
