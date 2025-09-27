package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "marca")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marca_id", nullable = false)
    private Integer marcaId;

    @Column(name = "nombre", columnDefinition = "text", nullable = false)
    private String nombre;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
