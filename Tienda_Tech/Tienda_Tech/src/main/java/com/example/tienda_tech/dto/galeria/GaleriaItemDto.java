// src/main/java/com/example/tienda_tech/dto/galeria/GaleriaItemDto.java
package com.example.tienda_tech.dto.galeria;

public record GaleriaItemDto(
    Integer galeriaId,
    Boolean esPortada,
    Boolean paraGaleria,
    Boolean paraMenu,
    Integer posicionGaleria,
    Integer posicionMenu,
    Boolean habilitado,
    String  descripcion,
    String  mimeType,
    Integer ancho,
    Integer alto,
    Long    pesoBytes
) {}
