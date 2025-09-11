// src/main/java/com/example/tienda_tech/controller/CategoriaController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.repository.CategoriaQueryRepository;
import org.springframework.web.bind.annotation.*;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
  private final CategoriaQueryRepository repo;

  public CategoriaController(CategoriaQueryRepository repo) { this.repo = repo; }

  @GetMapping
  public List<Map<String, Object>> listar() {
    return repo.listar().stream().map(r -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", r.getId_categoria());
      m.put("nombre", r.getNombre());
      m.put("slug", slug(r.getNombre()));
      return m;
    }).collect(Collectors.toList());
  }

  private static String slug(String s){
    String nfd = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}","");
    return nfd.replaceAll("[^\\w]+","-").toLowerCase().replaceAll("(^-|-$)","");
  }
}
