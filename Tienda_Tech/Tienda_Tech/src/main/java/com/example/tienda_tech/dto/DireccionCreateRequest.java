package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class DireccionCreateRequest {
    private String calle;
    private String referencia;
    private Short provinciaId;
    private Short ciudadId;
}
