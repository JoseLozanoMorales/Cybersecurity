// src/main/java/com/example/tienda_tech/service/UsuarioService.java
package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.UsuarioAdminDTO;
import com.example.tienda_tech.dto.UsuarioDTO;
import com.example.tienda_tech.dto.UsuarioMinDTO;
import com.example.tienda_tech.repository.UsuarioQueryRepository;
import com.example.tienda_tech.repository.UsuarioRepository;
import com.example.tienda_tech.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.tienda_tech.dto.ClienteUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.tienda_tech.repository.UsuarioQueryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    @Autowired
    private com.example.tienda_tech.repository.UsuarioRepository usuarioRepository;  // <-- UNO solo

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // NUEVO: para enviar credenciales por correo antes de registrar
    private final OtpService otpService;
    // NUEVO
    private final UsuarioQueryRepository usuarioQueryRepository;

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

    /* ======== NUEVO (admin/trabajador) por JSON con envío de credenciales ======== */
    @Transactional
    public void crearAdminOTrabajador(UsuarioDTO dto) {
        if (dto.getIdRol() == null || (dto.getIdRol() != 1 && dto.getIdRol() != 3)) {
            throw new IllegalArgumentException("idRol debe ser 1 (admin) o 3 (trabajador)");
        }
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio para enviar credenciales");
        }

        // 1) Generar y ENVIAR por correo (si falla -> excepción -> NO se registra)
        String passwordPlano = otpService.generarYEnviarCredenciales(
                dto.getCorreo(),
                dto.getUsuario(),
                12 // longitud sugerida
        );

        // 2) Hashear para BD
        String hash = passwordEncoder.encode(passwordPlano);

        // 3) Construir item JSON para SP
        UsuarioAdminDTO a = UsuarioAdminDTO.builder()
                .accion("AGREGAR")
                .nombre(dto.getNombre())
                .cedula(dto.getCedula())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .usuario(dto.getUsuario())
                .contrasenia(hash) // HASH al SP JSON
                .rolId(dto.getIdRol().intValue())
                .build();

        // 4) Enviar al SP (procedure o function con fallback)
        gestionarAdmins(List.of(a));
    }

    // NUEVO: buscar con la función ligera
    @Transactional(readOnly = true)
    public List<UsuarioMinDTO> buscarMin(String q, Integer rolId, int limit) {
        return usuarioQueryRepository.buscarMin(q, rolId, limit);
    }



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

    /*
     Envía el lote al SP/función JSON con fallback:
     Primero intenta CALL (procedure)
     Si falla, intenta SELECT (function)
     */
    @Transactional
    public void gestionarAdmins(List<UsuarioAdminDTO> items) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            String json = mapper.writeValueAsString(items);

            try {
                usuarioRepository.gestionarAdminsJsonCall(json);   // PROCEDURE
            } catch (Exception ignored) {
                usuarioRepository.gestionarAdminsJsonSelect(json); // FUNCTION
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Payload JSON inválido", e);
        }
    }

    /* ======== BÚSQUEDA (sin searchMin) ======== */
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorUsuario(String usuario, Integer rolId, int limit) {
        String q = usuario == null ? "" : usuario.trim();
        int lim = Math.max(1, Math.min(limit <= 0 ? 10 : limit, 50)); // 1..50

        List<Usuario> lista = (rolId == null)
                ? usuarioRepository.findTop50ByUsuarioContainingIgnoreCase(q)
                : usuarioRepository.findTop50ByUsuarioContainingIgnoreCaseAndIdRol(q, rolId);

        return lista.size() > lim ? lista.subList(0, lim) : lista;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

}
