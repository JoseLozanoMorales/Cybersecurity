package com.example.tienda_tech.model;

import jakarta.persistence.*;

// Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "direccion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"ciudad","usuario"})
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direccion_id")
    private Short direccionId;


    @Column(name = "calle")      private String calle;
    @Column(name = "referencia") private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id", referencedColumnName = "ciudad_id")
    private Ciudad ciudad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "habilitado")
    private Boolean habilitado;
}
