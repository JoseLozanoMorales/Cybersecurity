package com.example.tienda_tech.dto.galeria.product;

import com.example.tienda_tech.dto.galeria.GaleriaDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // oculta campos no usados en listado
public class CpuDTO {
    // Básico (listado)
    private Integer productoId;
    private String  nombre;
    private BigDecimal precio;   // preciounitario
    private Short   stock;
    private String  sockets;
    private Short   generacion;

    // Portada para la tarjeta
    private Long   portadaId;
    private String portadaUrl;   // ej: /api/galeria/{id}/thumb

    // Detalle (opcional)
    private String     enlace;
    private LocalDate  fecha;
    private Integer    marcaId;
    private Integer    gamaId;
    private Integer    ivaId;
    private BigDecimal costo;
    private Boolean    habilitado;
    private Integer    categoriaId;
    private BigDecimal valorInventario;

    // Solo para detalle
    private List<GaleriaDTO> galeria;
}
