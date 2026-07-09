// src/main/java/com/example/tienda_tech/dto/UsuarioDTO.java
package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Short  usuarioId;
    private String nombre;
    private String cedula;
    private String correo;
    private String telefono;
    private String contrasena;   // ← sin ñ (mapeamos al SP como v_contrasenia)
    private String usuario;

    // NUEVOS (para crear-usuario)
    private Short  idRol;         // 1=admin, 2=cliente, 3=trabajador
    private Short  idMetodoPago;  // opcional; puede ir null
}
