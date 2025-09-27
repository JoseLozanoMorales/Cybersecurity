package com.example.tienda_tech.service.audit.impl;

import com.example.tienda_tech.dto.audit.MovimientoAuditoriaDTO;
import com.example.tienda_tech.dto.audit.ProductoAuditoriaDTO;
import com.example.tienda_tech.dto.audit.UsuarioAuditoriaRespuestaDTO;
import com.example.tienda_tech.model.MovimientoInventarioAuditoria;
import com.example.tienda_tech.model.ProductoAuditoria;
import com.example.tienda_tech.repository.audit.MovimientoInventarioAuditoriaRepository;
import com.example.tienda_tech.repository.audit.ProductoAuditoriaRepository;
import com.example.tienda_tech.service.audit.AuditoriaService;
import com.example.tienda_tech.service.auth.UserOnlineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {

    private final ProductoAuditoriaRepository prodRepo;
    private final MovimientoInventarioAuditoriaRepository movRepo;
    private final UserOnlineService userOnlineService;
    private final ObjectMapper objectMapper; // Spring Boot auto-config

    @Override
    public List<String> listarUsuarios() {
        Set<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        s.addAll(prodRepo.usuariosUnicos());
        s.addAll(movRepo.usuariosUnicos());
        return new ArrayList<>(s);
    }

    @Override
    public List<ProductoAuditoriaDTO> buscarProductos(String usuario, Integer productoId,
                                                      LocalDate desde, LocalDate hasta) {
        String filtro = buildFiltroJson(usuario, productoId, desde, hasta);
        return prodRepo.buscarPorJson(filtro).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MovimientoAuditoriaDTO> buscarMovimientos(String usuario, Integer productoId,
                                                          LocalDate desde, LocalDate hasta) {
        String filtro = buildFiltroJson(usuario, productoId, desde, hasta);
        return movRepo.buscarPorJson(filtro).stream().map(this::toDTO).toList();
    }

    @Override
    public UsuarioAuditoriaRespuestaDTO auditoriaPorUsuario(String usuario,
                                                            LocalDate desde, LocalDate hasta) {
        var productos   = buscarProductos(usuario, null, desde, hasta);
        var movimientos = buscarMovimientos(usuario, null, desde, hasta);
        return UsuarioAuditoriaRespuestaDTO.builder()
                .usuario(usuario)
                .estadoOnline(userOnlineService.isOnline(usuario))
                .desde(desde)
                .hasta(hasta)
                .totalCambiosProducto(productos.size())
                .totalMovimientos(movimientos.size())
                .productos(productos)
                .movimientos(movimientos)
                .build();
    }

    // ---- helpers
    private String buildFiltroJson(String usuario, Integer productoId, LocalDate desde, LocalDate hasta) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (usuario != null && !usuario.isBlank()) m.put("usuario", usuario);
        if (productoId != null) m.put("productoId", productoId);
        if (desde != null) m.put("desde", desde.toString()); // YYYY-MM-DD
        if (hasta != null) m.put("hasta", hasta.toString());
        try { return objectMapper.writeValueAsString(m); }
        catch (Exception e) { throw new RuntimeException("No se pudo serializar filtro JSON", e); }
    }

    private ProductoAuditoriaDTO toDTO(ProductoAuditoria p){
        return ProductoAuditoriaDTO.builder()
                .fechahorareg(p.getFechaHoraReg())
                .usuario(p.getUsuario())
                .tipo(p.getTipo())
                .productoId(p.getProductoId())
                .nombre(p.getNombre())
                .nombreDespues(p.getNombreDespues())
                .precioUnitario(p.getPrecioUnitario())
                .precioUnitarioDespues(p.getPrecioUnitarioDespues())
                .stock(p.getStock() == null ? null : p.getStock().intValue())
                .stockDespues(p.getStockDespues() == null ? null : p.getStockDespues().intValue())
                .ivaId(p.getIvaId())
                .ivaIdDespues(p.getIvaIdDespues())
                .habilitado(p.getHabilitado())
                .habilitadoDespues(p.getHabilitadoDespues())
                .build();
    }

    private MovimientoAuditoriaDTO toDTO(MovimientoInventarioAuditoria m){
        return MovimientoAuditoriaDTO.builder()
                .fecha(m.getFecha())
                .fechahorareg(m.getFechaHoraReg())
                .usuario(m.getUsuario())
                .tipo(m.getTipo())
                .productoId(m.getProductoId())
                .subtipoId(m.getSubtipoId())
                .cantidad(m.getCantidad())
                .costoUnitario(m.getCostoUnitario())
                .costoTotal(m.getCostoTotal())
                .referencia(m.getReferencia())
                .observacion(m.getObservacion())
                .build();
    }
}
