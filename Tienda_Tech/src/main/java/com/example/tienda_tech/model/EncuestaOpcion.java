package com.example.tienda_tech.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "encuesta_opcion")
public class EncuestaOpcion {

    @Id
    @Column(name = "encuesta_id", nullable = false)
    private Short encuestaId;

    @Id
    @Column(name = "pregunta_key", length = 50, nullable = false)
    private String preguntaKey;

    @Id
    @Column(name = "valor", length = 100, nullable = false)
    private String valor;

    @Column(name = "texto", columnDefinition = "text", nullable = false)
    private String texto;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
