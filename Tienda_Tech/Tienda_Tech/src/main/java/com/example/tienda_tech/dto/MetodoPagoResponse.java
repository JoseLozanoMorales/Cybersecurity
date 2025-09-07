package com.example.tienda_tech.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetodoPagoResponse {
    private Integer id;
    private String mascara;     // "**** 1112"
    private String vence;       // "MM/YYYY"
    private Integer tipoId;
    private Boolean habilitado;
    private Boolean esPreferido;
}
