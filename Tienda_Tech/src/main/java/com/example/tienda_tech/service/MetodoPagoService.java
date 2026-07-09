package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.MetodoPagoDTO;
import com.example.tienda_tech.repository.MetodoPagoRepository;
import com.example.tienda_tech.security.SymmetricCryptoService;
import com.example.tienda_tech.util.MaskUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetodoPagoService {

    private final MetodoPagoRepository repo;
    private final ObjectMapper objectMapper;
    private final SymmetricCryptoService cryptoService;

    // ===== Lecturas =====
    public List<MetodoPagoDTO> listar(Integer userId) {
        return repo.listarPorUsuario(userId).stream().map(this::mapRowMetodo).toList();
    }

    public List<MetodoPagoDTO> listarTipos() {
        return repo.fnListarTipos().stream().map(this::mapRowTipo).toList();
    }

    // ===== Escrituras (SP) =====

    // ===== Escrituras =====
    @Transactional
    public void crear(Integer userId, MetodoPagoDTO req) {
        if (req.getTipoId() == null) throw new IllegalArgumentException("tipoId es obligatorio");
        ensureLuhn(req.getNumeroTarjeta());
        ensureMesFuturo(req.getMesExpiracion());

        // "YYYY-MM" -> fin de mes como DATE (ej. 2028-12-31)
        LocalDate fecha = YearMonth.parse(req.getMesExpiracion()).atEndOfMonth();

        repo.agregar(cryptoService.encrypt(req.getNumeroTarjeta()), fecha, req.getTipoId(), userId);
    }

    @Transactional
    public void eliminar(Integer userId, Integer metodoId) {
        // Si tu SP valida propiedad por UsuarioId, podrías agregar otra variante que lo incluya.
        repo.eliminar(metodoId);
    }


    // ===== Mappers =====
    private MetodoPagoDTO mapRowMetodo(Object[] r){
        // 0:id 1:numero_tarjeta cifrado o legado 2:fecha 3:habilitado 4:tipoId 5:tipoNombre
        String numeroTarjeta = cryptoService.decrypt(asString(r[1]));
        return MetodoPagoDTO.builder()
                .metodoId(asInt(r[0]))
                .mascara(MaskUtils.maskLast4(numeroTarjeta))
                .fechaExpiracion(asLocalDate(r[2]))
                .habilitado(asBool(r[3]))
                .tipoId(asInt(r[4]))
                .nombre(asString(r[5]))
                .build();
    }
    private MetodoPagoDTO mapRowTipo(Object[] r){
        // 0:tipo_id 1:nombre
        return MetodoPagoDTO.builder()
                .tipoId(asInt(r[0]))
                .nombre(asString(r[1]))
                .build();
    }

    private Integer asInt(Object o){ return o==null ? null : ((Number)o).intValue(); }
    private String asString(Object o){ return o==null ? null : o.toString(); }
    private Boolean asBool(Object o){
        if (o==null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue()!=0;
        return Boolean.parseBoolean(o.toString());
    }
    private LocalDate asLocalDate(Object o){
        if (o==null) return null;
        if (o instanceof LocalDate d) return d;
        if (o instanceof Date d2) return d2.toLocalDate();
        return LocalDate.parse(o.toString());
    }

    // ===== Validaciones =====
    private void ensureLuhn(String num) {
        if (num == null || !num.matches("\\d{12,19}") || !luhnOk(num))
            throw new IllegalArgumentException("Número de tarjeta inválido (Luhn)");
    }
    private boolean luhnOk(String s) {
        int sum=0; boolean dbl=false;
        for (int i=s.length()-1;i>=0;i--) { int k=s.charAt(i)-'0'; if(dbl){ k*=2; if(k>9)k-=9; } sum+=k; dbl=!dbl; }
        return sum%10==0;
    }
    private void ensureMesFuturo(String yyyyMm) {
        if (yyyyMm == null || !yyyyMm.matches("^\\d{4}-\\d{2}$"))
            throw new IllegalArgumentException("mesExpiracion debe ser YYYY-MM");
        if (YearMonth.parse(yyyyMm).isBefore(YearMonth.now()))
            throw new IllegalArgumentException("La tarjeta está expirada");
    }

    private String writeJsonArray(Map<String, Object> item) {
        try { return objectMapper.writeValueAsString(List.of(item)); }
        catch (Exception e) { throw new RuntimeException("Error serializando JSON para el SP", e); }
    }
}
