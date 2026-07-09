package com.example.tienda_tech.dto.encuesta;
public record OpcionUpsert(
        String key,
        String valor,
        String texto,
        Integer orden,
        Boolean habilitado) {}
