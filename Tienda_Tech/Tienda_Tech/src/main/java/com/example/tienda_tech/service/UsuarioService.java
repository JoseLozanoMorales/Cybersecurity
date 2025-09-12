// src/main/java/com/example/tienda_tech/service/UsuarioService.java
package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.AdminCreateRequest;
import com.example.tienda_tech.dto.ClienteUpdateRequest;
import com.example.tienda_tech.dto.UsuarioAdminDTO;
import com.example.tienda_tech.dto.UsuarioDTO;
import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

  private final UsuarioRepository usuarioRepo;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper mapper = new ObjectMapper();
  private final EmailService emailService; // define esta interfaz (ver abajo)

  /* ===== BÁSICOS ===== */

  public Usuario getById(Integer id) {
    return usuarioRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
  }

  public Usuario login(String usuario, String contraseniaPlain) {
    var opt = usuarioRepo.findByUsuario(usuario);
    if (opt.isEmpty()) throw new IllegalArgumentException("Credenciales inválidas");

    Usuario u = opt.get();
    String hash = u.getContrasenia();
    if (hash == null || !passwordEncoder.matches(contraseniaPlain, hash)) {
      throw new IllegalArgumentException("Credenciales inválidas");
    }
    return u;
  }

  /* ===== CLIENTE (flujo público existente) ===== */

  public void crearClienteConSP(UsuarioDTO dto) {
    String raw = dto.getContrasena();
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("La contraseña es obligatoria");
    }
    String hash = passwordEncoder.encode(raw);

    usuarioRepo.registrarClienteSP(
        dto.getNombre(),
        dto.getCedula(),
        dto.getCorreo(),
        dto.getTelefono(),
        dto.getUsuario(),
        hash
    );
  }

  public void actualizarCliente(Integer usuarioId, ClienteUpdateRequest req) {
    String nombre     = emptyToNull(req.getNombre());
    String cedula     = emptyToNull(req.getCedula());
    String correo     = emptyToNull(req.getCorreo());
    String telefono   = emptyToNull(req.getTelefono());
    String usuario    = emptyToNull(req.getUsuario());
    String contrasena = emptyToNull(req.getContrasena());

    if (contrasena != null) {
      contrasena = passwordEncoder.encode(contrasena);
    }

    usuarioRepo.actualizarClienteSP(
        usuarioId, nombre, cedula, correo, telefono, usuario, contrasena
    );
  }

  /* ===== ADMIN/TRABAJADOR – CREAR (JSON SP) ===== */

        @Transactional
    public void crearAdminOTrabajador(AdminCreateRequest req) {
    if (req.getIdRol() == null || !(req.getIdRol() == 1 || req.getIdRol() == 3)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido (solo 1=admin, 3=trabajador)");
    }

    // 1) Generar contraseña
    String plain = generarPasswordFuerte(12);
    String hash  = passwordEncoder.encode(plain);

    // 2) Construir OBJETO con claves que espera el SP
    ObjectNode obj = mapper.createObjectNode();
    obj.put("accion", "AGREGAR");               // ← usa la palabra que tu SP espera
    obj.putNull("usuario_id");
    obj.put("nombre",   req.getNombre());
    obj.put("cedula",   req.getCedula());
    obj.put("correo",   req.getCorreo());
    obj.put("telefono", req.getTelefono());
    obj.put("usuario",  (req.getUsuario()==null || req.getUsuario().isBlank())
                        ? sugerirUsuario(req.getNombre(), req.getCorreo())
                        : req.getUsuario());

    // >>> Elige UNO: si el SP hashea internamente usa 'plain'; si no, envía 'hash'
    obj.put("contrasenia", hash);               // ó: obj.put("contrasenia", plain);

    obj.put("rol_id",  req.getIdRol());

    // 3) Envolver en ARREGLO (el SP usa jsonb_array_elements)
    String arrJson = "[" + obj.toString() + "]";

    try {
        // 4) Llamar PROCEDURE con fallback a FUNCTION
        try {
        usuarioRepo.gestionarAdminsJsonCall(arrJson);
        } catch (Exception ignored) {
        usuarioRepo.gestionarAdminsJsonSelect(arrJson);
        }

        // 5) Enviar correo con credenciales
        emailService.enviarCredenciales(req.getCorreo(), obj.get("usuario").asText(), plain);

    } catch (DataIntegrityViolationException dup) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario/correo/cédula ya existen", dup);
    } catch (ResponseStatusException ex) {
        throw ex;
    } catch (Exception ex) {
        log.error("Error creando usuario admin/trabajador", ex);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear usuario", ex);
    }
}



  /* ===== ADMIN/TRABAJADOR – ACTUALIZAR/DESHABILITAR (JSON SP por lote) ===== */

  @Transactional
  public void actualizarAdmin(Integer id, Integer rolId, ClienteUpdateRequest req) {
    String contrasenia = emptyToNull(req.getContrasena());
    if (contrasenia != null) contrasenia = passwordEncoder.encode(contrasenia);

    UsuarioAdminDTO a = UsuarioAdminDTO.builder()
        .accion("ACTUALIZAR")
        .usuarioId(id)
        .rolId(rolId)
        .nombre(emptyToNull(req.getNombre()))
        .cedula(emptyToNull(req.getCedula()))
        .correo(emptyToNull(req.getCorreo()))
        .telefono(emptyToNull(req.getTelefono()))
        .usuario(emptyToNull(req.getUsuario()))
        .contrasenia(contrasenia) // null => no cambia
        .build();

    gestionarAdmins(List.of(a));
  }

  @Transactional
  public void deshabilitarAdmin(Integer id, Integer rolId) {
    UsuarioAdminDTO a = UsuarioAdminDTO.builder()
        .accion("DESHABILITAR")
        .usuarioId(id)
        .rolId(rolId)
        .build();

    gestionarAdmins(List.of(a));
  }

  @Transactional
  public void gestionarAdmins(List<UsuarioAdminDTO> items) {
    try {
      ObjectMapper snake = new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      String json = snake.writeValueAsString(items);
      try {
        usuarioRepo.gestionarAdminsJsonCall(json);   // PROCEDURE
      } catch (Exception ignored) {
        usuarioRepo.gestionarAdminsJsonSelect(json); // FUNCTION (si aplica)
      }
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Payload JSON inválido", e);
    }
  }

  /* ===== BÚSQUEDA ===== */

  @Transactional(readOnly = true)
  public List<Usuario> buscarPorUsuario(String usuario, Integer rolId, int limit) {
    String q = (usuario == null) ? "" : usuario.trim();
    int lim = Math.max(1, Math.min(limit <= 0 ? 10 : limit, 50)); // 1..50
    List<Usuario> lista = (rolId == null)
        ? usuarioRepo.findTop50ByUsuarioContainingIgnoreCase(q)
        : usuarioRepo.findTop50ByUsuarioContainingIgnoreCaseAndIdRol(q, rolId);
    return lista.size() > lim ? lista.subList(0, lim) : lista;
  }

  /* ===== Helpers ===== */

  private static String emptyToNull(String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
  }

  private static String generarPasswordFuerte(int len) {
    final String A = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    final String a = "abcdefghijkmnpqrstuvwxyz";
    final String d = "23456789";
    final String s = "@#$%*+-_";
    final String all = A + a + d + s;
    SecureRandom r = new SecureRandom();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) sb.append(all.charAt(r.nextInt(all.length())));
    return sb.toString();
  }

  private static String sugerirUsuario(String nombre, String correo) {
    if (correo != null && correo.contains("@")) {
      return correo.substring(0, correo.indexOf('@'));
    }
    String base = (nombre == null ? "user" : nombre.trim().replaceAll("\\s+", "")).toLowerCase();
    return base.length() > 12 ? base.substring(0, 12) : base;
  }
}
