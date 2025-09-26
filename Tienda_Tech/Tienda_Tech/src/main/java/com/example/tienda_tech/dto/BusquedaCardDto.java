package com.example.tienda_tech.dto;

import java.math.BigDecimal;

public record BusquedaCardDto(
        Integer productoId,
        String  nombre,
        BigDecimal preciounitario,
        String  marca,
        Integer imagenId
) {}
