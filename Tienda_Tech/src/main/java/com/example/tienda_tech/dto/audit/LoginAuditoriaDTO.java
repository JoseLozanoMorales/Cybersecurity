package com.example.tienda_tech.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class LoginAuditoriaDTO {
    private Integer idSesion;
    private Integer usuarioId;
    private String  usuario;      // nombre
    private String  ip;
    private String  host;
    private OffsetDateTime fechaLogin;

}
