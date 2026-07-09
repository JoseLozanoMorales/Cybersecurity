package com.example.tienda_tech.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carrito_detalle")
public class CarritoDetalle {

    @Id
    @Column(name = "carrito_id", nullable = false)
    private Integer carritoId;

    @Id
    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

}
