package com.example.tienda_tech.model;

import jakarta.persistence.*;

// Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "provincia")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString // puedes quitar el exclude viejo de "direcciones"
public class Provincia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provincia_id")
    private Short provinciaId;

    @Column(name = "nombre")
    private String nombre;
}

