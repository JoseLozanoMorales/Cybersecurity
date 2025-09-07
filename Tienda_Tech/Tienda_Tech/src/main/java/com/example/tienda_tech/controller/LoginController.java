// src/main/java/com/example/tienda_tech/controller/LoginController.java
package com.example.tienda_tech.controller;

import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

  @Autowired
  private UsuarioService usuarioService;

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
    String usuario = body.get("usuario");
    String contrasenia = body.get("contrasena");

    var u = usuarioService.login(usuario, contrasenia);

    Map<String, Object> userPayload = Map.of(
        "usuarioId", u.getUsuarioId(),
        "usuario",   u.getUsuario(),
        "nombre",    u.getNombre(),
        "correo",    u.getCorreo(),
        "telefono",  u.getTelefono(),
        "id_rol",    u.getIdRol()
    );
    return ResponseEntity.ok(Map.of("success", true, "user", userPayload, "token", "mock"));
}

}
