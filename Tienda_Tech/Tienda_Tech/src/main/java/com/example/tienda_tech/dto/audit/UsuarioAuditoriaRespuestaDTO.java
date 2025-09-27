package com.example.tienda_tech.dto.audit;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioAuditoriaRespuestaDTO {
    private String usuario;
    private boolean estadoOnline;

    private LocalDate desde;
    private LocalDate hasta;

    private int totalCambiosProducto;
    private int totalMovimientos;

    private List<ProductoAuditoriaDTO> productos;
    private List<MovimientoAuditoriaDTO> movimientos;
}
