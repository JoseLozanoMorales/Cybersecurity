package com.example.tienda_tech.model;

import jakarta.persistence.*;
import java.util.List;

// Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "direcciones")
public class Usuario {

    @Id
    @Column(name = "usuario_id") // así está la BD
    private Integer usuarioId;

    @Column(name = "nombre")     private String nombre;
    @Column(name = "cedula")     private String cedula;
    @Column(name = "correo")     private String correo;
    @Column(name = "telefono")   private String telefono;
    @Column(name = "usuario")    private String usuario;    // username
    @Column(name = "contrasenia")private String contrasenia; // sin ñ

    @Column(name = "usuario_bd",     nullable = false, length = 50)
    private String usuarioBd;

    @Column(name = "contrasenia_bd", nullable = false, length = 255)
    private String contraseniaBd;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = Boolean.TRUE;

    @Column(name = "metodopago_id") private Short idMetodoPago;
    @Column(name = "rol_id")        private Short idRol;

    @Column(name = "avatar_path")
    private String avatarPath;

    // 1 -> N (un usuario tiene muchas direcciones)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Direccion> direcciones;
}
