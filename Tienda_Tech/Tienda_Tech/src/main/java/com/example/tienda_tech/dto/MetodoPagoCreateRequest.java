package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class MetodoPagoCreateRequest {
    private String numeroTarjeta;       // "4111111111111111"
    private String fechaExpiracion;     // "2027-10-01" (YYYY-MM-DD)
    private Integer tipoId;             // 1,2,3...
    private Boolean preferido;          // opcional: si true, marcar como predeterminado tras crear
}
