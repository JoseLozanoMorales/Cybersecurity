package com.example.tienda_tech.service.audit;

import com.example.tienda_tech.dto.audit.LoginAuditoriaDTO;
import com.example.tienda_tech.repository.audit.UsuarioAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioAuditoriaService {

    private final UsuarioAuditoriaRepository repo;

    @Transactional
    public void registrarLogin(Integer usuarioId) {
        repo.auditarLogin(usuarioId);   // el SP inserta ip, host y fecha automáticamente
    }

    // ===== nuevos (solo lectura) =====
    @Transactional(readOnly = true)
    public List<String> listarUsuariosVisibles() {
        return repo.fnUsuarios();
    }

    @Transactional(readOnly = true)
    public List<LoginAuditoriaDTO> buscarLogins(String usuario, LocalDate desde, LocalDate hasta) {
        List<Object[]> rows = repo.fnLoginsRaw(
                (usuario == null || usuario.isBlank()) ? "" : usuario.trim(),
                desde, hasta
        );
        return rows.stream().map(this::toDto).toList();
    }

   // ===== mapeo seguro Object[] -> DTO =====
    private LoginAuditoriaDTO toDto(Object[] r) {
        Integer idSesion  = asInt(r[0]);
        Integer usuarioId = asInt(r[1]);
        String  usuario   = (String)  r[2];
        String  ip        = (String)  r[3];
        String  host      = (String)  r[4];
        OffsetDateTime fecha = asOffsetDateTime(r[5]);
        return new LoginAuditoriaDTO(idSesion, usuarioId, usuario, ip, host, fecha);
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Long l) return Math.toIntExact(l);
        if (o instanceof java.math.BigInteger bi) return bi.intValue();
        if (o instanceof Short s) return (int) s;
        return Integer.valueOf(o.toString());
    }

    private static OffsetDateTime asOffsetDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof OffsetDateTime odt) return odt;
        if (o instanceof java.sql.Timestamp ts) return ts.toInstant().atOffset(java.time.ZoneOffset.UTC);
        if (o instanceof java.time.LocalDateTime ldt) return ldt.atOffset(java.time.ZoneOffset.UTC);
        if (o instanceof java.util.Date d) return d.toInstant().atOffset(java.time.ZoneOffset.UTC);
        return OffsetDateTime.parse(o.toString());
    }
}
