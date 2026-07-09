package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.SugerenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sugerencias")
@RequiredArgsConstructor
public class SugerenciaController {

    private final SugerenciaService service;
    private final ObjectMapper mapper;

    @PostMapping("/generar")
    public ResponseEntity<?> generar(@RequestBody Map<String, Object> body) {
        try {
            String usuario = Optional.ofNullable((String) body.get("usuario"))
                    .orElseGet(this::currentUsername);

            int encuestaId = ((Number) body.getOrDefault("encuestaId", 1)).intValue();
            int topN       = ((Number) body.getOrDefault("topN", 3)).intValue();
            double presupuesto = Optional.ofNullable((Number) body.get("presupuesto"))
                    .map(Number::doubleValue)
                    .orElse(0d);
            boolean warnLowBudget = presupuesto >= 0 && presupuesto < 100;

            // payload completo que se envía al SP (como JSON string)
            String payload = mapper.valueToTree(body).toString();

            int sugId = service.generarSugerencia(payload, usuario, topN, encuestaId);

            // Detalle crudo desde BD
            List<Map<String,Object>> items = service.detalle(sugId);

            // Adaptación de claves para la UI (producto / precio / compat / motivo)
            List<Map<String, Object>> itemsUi = items.stream().map(m -> {
                Map<String, Object> x = new LinkedHashMap<>();
                x.put("categoria",     m.getOrDefault("categoria", "OTROS"));
                x.put("producto",      m.getOrDefault("nombre", ""));
                x.put("precio",        m.getOrDefault("preciounitario", 0));
                x.put("compat",        true);   // placeholder
                x.put("motivo",        "");     // placeholder
                // campos útiles adicionales
                x.put("producto_id",   m.get("producto_id"));
                x.put("id_categoria",  m.get("id_categoria"));
                x.put("gama",          m.get("gama"));
                return x;
            }).collect(Collectors.toList()); // usa collect para Java 8

            // Total y agrupación sobre itemsUi (no sobre items)
            double total = itemsUi.stream()
                    .map(i -> (Number) i.getOrDefault("precio", 0))
                    .mapToDouble(Number::doubleValue)
                    .sum();

            Map<String, List<Map<String,Object>>> byCat = itemsUi.stream()
                    .collect(Collectors.groupingBy(
                            i -> Objects.toString(i.get("categoria"), "OTROS"),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("sugerenciaId",      sugId);
            resp.put("items",             itemsUi);     // lo que la UI espera
            resp.put("categorias",        byCat);       // agrupado opcional para otra vista
            resp.put("totalPrecio",       total);
            resp.put("totalComponentes",  itemsUi.size());
            resp.put("tipo",              "pc-completa");
            resp.put("presupuesto",       ((Number) body.getOrDefault("presupuesto", 0)).doubleValue());
            if (warnLowBudget) {
                resp.put("warnings", List.of(
                        Map.of("code", "LOW_BUDGET", "message", "Presupuesto muy bajo")
                ));
            }
            return ResponseEntity.ok(resp);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(422).body(Map.of(
                    "error", "Integridad referencial",
                    "detalle", e.getMostSpecificCause() != null
                            ? e.getMostSpecificCause().getMessage()
                            : e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error interno",
                    "detalle", Optional.ofNullable(deepSqlMsg(e)).orElse(e.getMessage())
            ));
        }
    }

    private String currentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.getName() != null) ? auth.getName() : "anon";
        } catch (Exception e) {
            return "anon";
        }
    }

    /** Busca en la cadena de causas un PSQLException (o similar) y devuelve su mensaje. */
    private static String deepSqlMsg(Throwable t) {
        while (t != null) {
            String cn = t.getClass().getName();
            if (cn.contains("PSQLException") || cn.contains("SQLException")) {
                return t.getMessage();
            }
            t = t.getCause();
        }
        return null;
    }
}
