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
@Table(name = "perifericos")
public class Periferico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "preciounitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "enlace", length = 255)
    private String enlace;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "stock", nullable = false)
    private Short stock; // smallint (DEFAULT 0)

    @Column(name = "marca_id", nullable = false)
    private Integer marcaId;

    @Column(name = "gama_id")
    private Integer gamaId;

    @Column(name = "iva_id", nullable = false)
    private Integer ivaId;

    @Column(name = "costo", precision = 10, scale = 2, nullable = false)
    private BigDecimal costo;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado; // DEFAULT true

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "valor_inventario", precision = 18, scale = 2, nullable = false)
    private BigDecimal valorInventario; // DEFAULT 0

    @Column(name = "tipo", columnDefinition = "text")
    private String tipo;
}
