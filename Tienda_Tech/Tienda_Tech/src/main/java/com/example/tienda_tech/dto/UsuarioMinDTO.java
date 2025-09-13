package com.example.tienda_tech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UsuarioMinDTO {
    private Integer usuarioId;
    private String  nombre;
    private String  cedula;
    private String  correo;
    private String  telefono;
    private String  usuario;
    private Integer rolId;
    private Boolean habilitado;
}
