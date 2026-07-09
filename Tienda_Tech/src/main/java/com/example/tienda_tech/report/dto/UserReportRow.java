package com.example.tienda_tech.report.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReportRow {
    private Integer usuarioId;
    private String  nombre;
    private String  usuario;
    private String  correo;
    private String  telefono;
    private String  rol;       // join a rol
    private String  estado;    // Activo/Inactivo
}
