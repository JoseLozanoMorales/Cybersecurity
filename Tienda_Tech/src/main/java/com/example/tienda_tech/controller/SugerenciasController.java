package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.encuesta.*;
import com.example.tienda_tech.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SugerenciasController {
    private final SuggestionRepository repo;

    @PostMapping("/sugerencias")
    public Map<String,Object> crear(@RequestBody SugerenciaCreate b) throws Exception {
        repo.createV3(b.respuestas(), b.topN(), b.encuestaId(), b.usuarioFallback());
        // Si quieres devolver la última del usuario, usa getLastForUser aquí cuando venga usuario_id
        return Map.of("status","ok");
    }

    @GetMapping("/sugerencias/{id}")
    public Map<String,Object> get(@PathVariable Integer id) throws Exception {
        return repo.getResult(id);
    }

    @GetMapping("/usuarios/{usuarioId}/sugerencias/ultima")
    public Map<String,Object> last(@PathVariable Integer usuarioId) throws Exception {
        return repo.getLastForUser(usuarioId);
    }

    @PutMapping("/sugerencias/{id}")
    public Map<String,Object> actualizar(@PathVariable Integer id, @RequestBody SugerenciaUpdate b) throws Exception {
        repo.updateV3(id, b.respuestas(), b.topN(), b.encuestaId(), b.merge());
        return repo.getResult(id);
    }

    @PostMapping("/sugerencias/{id}:toggle")
    public void toggle(@PathVariable Integer id, @RequestBody ToggleBody b){
        repo.toggle(id, b.deshabilitar());
    }

    @DeleteMapping("/sugerencias/{id}")
    public void delete(@PathVariable Integer id){
        repo.deleteIfDisabled(id);
    }

    @GetMapping("/usuarios/{usuarioId}/sugerencias")
    public Map<String,Object> list(@PathVariable Integer usuarioId,
                                   @RequestParam(required=false) Integer limit,
                                   @RequestParam(required=false) Integer offset) throws Exception {
        return repo.listForUser(usuarioId, limit, offset);
    }
    @PostMapping("/sugerencias/full")
    public Map<String,Object> crearFull(@RequestBody Map<String,Object> body) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String,Object> respuestas = (Map<String,Object>) body.get("respuestas");
        String usuarioFallback = (String) body.getOrDefault("usuarioFallback", null);
        int topN          = body.get("topN")       == null ? 3 : ((Number) body.get("topN")).intValue();
        int encuestaIdInt = body.get("encuestaId") == null ? 1 : ((Number) body.get("encuestaId")).intValue();

        return repo.createAndReturn(respuestas, topN, encuestaIdInt, usuarioFallback);
    }


}
