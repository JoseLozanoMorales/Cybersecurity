package com.example.tienda_tech.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetodoPagoDTO {

    // Respuesta (listar)
    private Integer metodoId;          // metodopago_id
    private String  mascara;           // '**** 1234'
    private LocalDate fechaExpiracion; // YYYY-MM-DD
    private Boolean habilitado;

    // Tipo
    private Integer tipoId;
    private String  nombre;            // nombre del tipo

    // Crear (request)
    private String numeroTarjeta;      // 12–19 dígitos sin espacios
    private String mesExpiracion;      // "YYYY-MM" (input type="month")
}
