package com.example.tienda_tech.controller;

import com.example.tienda_tech.dto.encuesta.*;
import com.example.tienda_tech.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/encuesta")
@RequiredArgsConstructor
public class EncuestaAdminController {
    private final SurveyRepository repo;

    @GetMapping
    public Map<String,Object> get() throws Exception { return repo.getSurveyJson(); }

    @PutMapping
    public void update(@RequestBody EncuestaUpdate b){ repo.updateEncuesta(b); }

    @PostMapping("/preguntas")
    public void upsertPregunta(@RequestBody PreguntaUpsert b){ repo.upsertPregunta(b); }

    @DeleteMapping("/preguntas/{key}")
    public void deletePregunta(@PathVariable String key){ repo.deletePregunta(key); }

    @PostMapping("/opciones")
    public void upsertOpcion(@RequestBody OpcionUpsert b){ repo.upsertOpcion(b); }

    @DeleteMapping("/opciones/{key}/{valor}")
    public void deleteOpcion(@PathVariable String key, @PathVariable String valor){ repo.deleteOpcion(key, valor); }

    @PostMapping("/reglas")
    public void upsertRegla(@RequestBody ReglaUpsert b){ repo.upsertRegla(b); }

    @DeleteMapping("/reglas/{key}/{valor}/{categoria}")
    public void deleteRegla(@PathVariable String key, @PathVariable String valor, @PathVariable String categoria){
        repo.deleteRegla(key, valor, categoria);
    }
}
