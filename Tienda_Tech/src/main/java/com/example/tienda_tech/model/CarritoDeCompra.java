package com.example.tienda_tech.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carrito_de_compra")
public class CarritoDeCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrito_id", nullable = false)
    private Integer carritoId;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;

    @PrePersist
    public void prePersist() {
        if (total == null) total = BigDecimal.ZERO;
        if (habilitado == null) habilitado = Boolean.TRUE;
    }
}
