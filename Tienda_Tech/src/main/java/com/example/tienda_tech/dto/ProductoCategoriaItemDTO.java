// src/main/java/com/example/tienda_tech/dto/ProductoCategoriaItemDTO.java
package com.example.tienda_tech.dto;

import java.math.BigDecimal;

public record ProductoCategoriaItemDTO(
  Integer id,
  String nombre,
  BigDecimal precio,
  String marca,
  Integer imagenId,
  String mimeType
) {}
