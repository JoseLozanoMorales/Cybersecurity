package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resenias")
public class Resenia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resenia_id", nullable = false)
    private Integer reseniaId;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "texto", columnDefinition = "text", nullable = false)
    private String texto;

    @Column(name = "valoracion", nullable = false)
    private Short valoracion;               // smallint

    @Column(name = "orden_id", nullable = false)
    private Integer ordenId;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "habilitado")
    private Boolean habilitado;
}
