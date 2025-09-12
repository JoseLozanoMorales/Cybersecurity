// src/main/java/com/example/tienda_tech/dto/UsuarioAdminDTO.java
package com.example.tienda_tech.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioAdminDTO {
  private String accion;     // "CREAR" | "ACTUALIZAR" | "DESHABILITAR"
  private Integer usuarioId; // null en CREAR
  private String  nombre;
  private String  cedula;
  private String  correo;
  private String  telefono;
  private String  usuario;
  private String  contrasenia; // hash o null si no cambia
  private Integer rolId;       // 1 o 3
}
