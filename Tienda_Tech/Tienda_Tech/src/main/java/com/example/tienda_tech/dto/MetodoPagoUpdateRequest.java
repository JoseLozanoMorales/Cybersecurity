package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class MetodoPagoUpdateRequest {
    private String numeroTarjeta;       // opcional
    private String fechaExpiracion;     // opcional (YYYY-MM-DD)
    private Integer tipoId;             // opcional
    private Boolean habilitado;         // opcional
}
