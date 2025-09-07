// src/main/java/com/example/tienda_tech/service/UsuarioService.java
package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.UsuarioDTO;
import com.example.tienda_tech.repository.UsuarioRepository;
import com.example.tienda_tech.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.tienda_tech.dto.ClienteUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    @Autowired
    private com.example.tienda_tech.repository.UsuarioRepository usuarioRepository;  // <-- UNO solo

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public com.example.tienda_tech.model.Usuario getById(Integer id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new com.example.tienda_tech.exception.NotFoundException("Usuario no encontrado"));
        // Si no tienes NotFoundException, usa:
        // .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }



    public Usuario login(String usuario, String contraseniaPlain) {
    var opt = usuarioRepository.findByUsuario(usuario);
    if (opt.isEmpty()) throw new IllegalArgumentException("Credenciales inválidas");

    Usuario u = opt.get();
    String hash = u.getContrasenia(); // la columna actualmente se llama así
    if (hash == null || !passwordEncoder.matches(contraseniaPlain, hash)) {
        throw new IllegalArgumentException("Credenciales inválidas");
    }
    return u;
    }


    // Ya existente: registro público (cliente)
    public void crearClienteConSP(UsuarioDTO dto) {
        String raw = dto.getContrasena();
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        String hash = passwordEncoder.encode(raw);            // <<--- HASH AQUÍ

        usuarioRepository.registrarClienteSP(
            dto.getNombre(),
            dto.getCedula(),
            dto.getCorreo(),
            dto.getTelefono(),
            dto.getUsuario(),
            hash                              // <<--- ENVIAR HASH
        );
    }


    // NUEVO: panel admin — crea cualquier rol con SP crear_usuario
    public void crearUsuarioConSP(UsuarioDTO dto) {
        Integer idMetodoPago = dto.getIdMetodoPago() != null ? dto.getIdMetodoPago().intValue() : null;
        Integer idRol        = dto.getIdRol() != null        ? dto.getIdRol().intValue()        : 2; // por defecto cliente

        usuarioRepository.crearUsuarioSP(
                dto.getNombre(),
                dto.getCedula(),
                dto.getCorreo(),
                dto.getTelefono(),
                dto.getContrasena(), // mapea a v_contrasenia
                dto.getUsuario(),
                idMetodoPago,
                idRol
        );
    }
    public void actualizarCliente(Integer usuarioId, ClienteUpdateRequest req) {
        String nombre    = emptyToNull(req.getNombre());
        String cedula    = emptyToNull(req.getCedula());
        String correo    = emptyToNull(req.getCorreo());
        String telefono  = emptyToNull(req.getTelefono());
        String usuario   = emptyToNull(req.getUsuario());
        String contrasenia = emptyToNull(req.getContrasena());

        // Si el front manda nueva contraseña, hasheamos
        if (contrasenia != null) {
            contrasenia = passwordEncoder.encode(contrasenia);
        }

        usuarioRepository.actualizarClienteSP(
            usuarioId, nombre, cedula, correo, telefono, usuario, contrasenia
        );
    }


    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

}
