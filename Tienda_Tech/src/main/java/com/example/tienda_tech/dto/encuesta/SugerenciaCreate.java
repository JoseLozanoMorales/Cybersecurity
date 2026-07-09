package com.example.tienda_tech.dto.encuesta;
import java.util.Map;
public record SugerenciaCreate(Map<String,Object> respuestas, Integer topN, Integer encuestaId, String usuarioFallback) {}
