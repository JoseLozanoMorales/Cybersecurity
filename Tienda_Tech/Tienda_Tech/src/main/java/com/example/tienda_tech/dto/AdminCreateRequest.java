// src/main/java/com/example/tienda_tech/dto/AdminCreateRequest.java
package com.example.tienda_tech.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateRequest {
  @NotBlank private String nombre;
  @NotBlank @Size(min = 10, max = 10) private String cedula;
  @NotBlank @Email private String correo;
  @NotBlank @Size(min = 10, max = 10) private String telefono;
  private String usuario;           // opcional
  @NotNull private Integer idRol;   // 1=admin, 3=trabajador
}
