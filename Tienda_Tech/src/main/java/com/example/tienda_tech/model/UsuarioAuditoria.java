package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_auditoria")
public class UsuarioAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion", nullable = false)
    private Integer idSesion;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "ip", nullable = false, columnDefinition = "inet")
    private String ip;

    @Column(name = "nombre_ordenador", nullable = false, columnDefinition = "text")
    private String nombreOrdenador;

    @CreationTimestamp
    @Column(name = "fecha_login", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime fechaLogin;

}
