package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "galeria_productos_v2")
public class GaleriaProductoV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // bigint autoincremental
    @Column(name = "galeria_id", nullable = false)
    private Long galeriaId;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;

    @Column(name = "es_portada", nullable = false)
    private Boolean esPortada;

    @Column(name = "para_galeria", nullable = false)
    private Boolean paraGaleria;

    @Column(name = "para_menu", nullable = false)
    private Boolean paraMenu;

    @Column(name = "posicion_galeria")
    private Integer posicionGaleria;

    @Column(name = "posicion_menu")
    private Integer posicionMenu;

    @Column(name = "mime_type", length = 64)
    private String mimeType;

    @Column(name = "peso_bytes")
    private Long pesoBytes;

    @Column(name = "ancho")
    private Integer ancho;

    @Column(name = "alto")
    private Integer alto;
}
