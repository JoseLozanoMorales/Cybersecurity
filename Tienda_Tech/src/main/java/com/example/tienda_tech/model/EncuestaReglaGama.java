package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "encuesta_regla_gama")
public class EncuestaReglaGama {

    @Id
    @Column(name = "encuesta_id", nullable = false)
    private Short encuestaId;

    @Id
    @Column(name = "pregunta_key", length = 50, nullable = false)
    private String preguntaKey;

    @Id
    @Column(name = "valor", length = 100, nullable = false)
    private String valor;

    @Id
    @Column(name = "categoria", length = 20, nullable = false)
    private String categoria;

    @Column(name = "gama_objetivo", length = 10, nullable = false)
    private String gamaObjetivo;

    @Column(name = "delta_rank", nullable = false)
    private Short deltaRank;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
