package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "encuesta")
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "encuesta_id", nullable = false)
    private Short encuestaId;

    @Column(name = "nombre", nullable = false, columnDefinition = "text")
    private String nombre;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
