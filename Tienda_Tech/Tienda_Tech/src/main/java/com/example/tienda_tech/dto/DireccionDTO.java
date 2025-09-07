package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class DireccionDTO {
    private Integer direccionId;
    private Long usuarioId;
    private String calle;
    private String referencia;
    private String ciudadNombre;
}
