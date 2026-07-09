package com.example.tienda_tech.dto.encuesta;
import java.util.Map;
public record SugerenciaUpdate(Map<String,Object> respuestas, Integer topN, Integer encuestaId, Boolean merge) {}
