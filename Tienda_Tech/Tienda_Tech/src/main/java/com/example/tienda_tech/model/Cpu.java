package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cpu")
public class Cpu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Integer productoId;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "preciounitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "enlace", length = 255)
    private String enlace;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "stock")
    private Short stock; // smallint

    @Column(name = "marca_id", nullable = false)
    private Integer marcaId;

    @Column(name = "gama_id")
    private Integer gamaId;

    @Column(name = "iva_id", nullable = false)
    private Integer ivaId;

    @Column(name = "costo", precision = 10, scale = 2, nullable = false)
    private BigDecimal costo;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = Boolean.TRUE;

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "valor_inventario", precision = 18, scale = 2, nullable = false)
    private BigDecimal valorInventario = BigDecimal.ZERO;

    @Column(name = "sockets", length = 10)
    private String sockets;

    @Column(name = "generacion")
    private Short generacion;

}
