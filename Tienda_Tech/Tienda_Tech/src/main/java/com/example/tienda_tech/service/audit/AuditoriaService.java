package com.example.tienda_tech.service.audit;

import com.example.tienda_tech.dto.audit.*;

import java.time.LocalDate;
import java.util.List;

public interface AuditoriaService {
    List<String> listarUsuarios();

    List<ProductoAuditoriaDTO> buscarProductos(String usuario, Integer productoId,
                                               LocalDate desde, LocalDate hasta);

    List<MovimientoAuditoriaDTO> buscarMovimientos(String usuario, Integer productoId,
                                                   LocalDate desde, LocalDate hasta);

    UsuarioAuditoriaRespuestaDTO auditoriaPorUsuario(String usuario,
                                                     LocalDate desde, LocalDate hasta);
}
