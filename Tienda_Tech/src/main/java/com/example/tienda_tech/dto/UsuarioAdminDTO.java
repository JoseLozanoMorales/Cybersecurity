package com.example.tienda_tech.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UsuarioAdminDTO {
    private String  accion;       // "AGREGAR" | "ACTUALIZAR" | "DESHABILITAR"
    private Integer usuarioId;    // requerido en ACTUALIZAR / DESHABILITAR
    private String  nombre;
    private String  cedula;
    private String  correo;
    private String  telefono;
    private String  usuario;
    private String  contrasenia;  // (sin ñ)
    private Integer rolId;        // 1=admin, 3=trabajador
}
