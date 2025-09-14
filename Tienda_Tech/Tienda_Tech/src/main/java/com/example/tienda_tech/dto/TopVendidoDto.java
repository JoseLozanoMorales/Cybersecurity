package com.example.tienda_tech.dto;

import java.math.BigDecimal;

public record TopVendidoDto(
  int productoId,
  String nombre,
  BigDecimal precio,
  long ventas,
  Integer galeriaId,
  String urlImagen
) {}
