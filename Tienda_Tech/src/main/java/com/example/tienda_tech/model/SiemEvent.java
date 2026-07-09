package com.example.tienda_tech.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiemEvent {
    private String fecha;
    private String tipo;
    private String usuario;
    private String ip;
    private String modulo;
    private String resultado;
    private String nivel; // INFO, ADVERTENCIA, ALERTA
    private String detalle;
}

