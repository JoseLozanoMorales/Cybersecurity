package com.example.tienda_tech.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DireccionDTO {
    private Short direccionId;
    private Integer usuarioId;        // viene en el path, lo devolvemos al listar
    private String  calle;
    private String  referencia;
    private Short   ciudadId;

    // para pintar (opcionales)
    private String  ciudadNombre;
    private String  provinciaNombre;
    private Boolean habilitado;
}
