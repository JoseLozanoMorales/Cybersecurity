package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sugerencia")
public class Sugerencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sugerencia_id", nullable = false)
    private Integer sugerenciaId;

    @Column(name = "gama", length = 5)
    private String gama;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
