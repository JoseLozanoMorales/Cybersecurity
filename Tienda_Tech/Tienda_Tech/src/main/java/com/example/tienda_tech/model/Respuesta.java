package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "respuesta")
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "respuesta_id", nullable = false)
    private Integer respuestaId;

    @Column(name = "sugerencia_id")
    private Integer sugerenciaId;

    @Column(name = "pregunta_texto", columnDefinition = "text")
    private String preguntaTexto;

    @Column(name = "respuesta_texto", columnDefinition = "text")
    private String respuestaTexto;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "pregunta_key", length = 50)
    private String preguntaKey;

    @Column(name = "opcion_valor", length = 100)
    private String opcionValor;
}
