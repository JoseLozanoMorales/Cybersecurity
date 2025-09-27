package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gama")
public class Gama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gama_id", nullable = false)
    private Integer gamaId;

    @Column(name = "tipo_gama", length = 5, nullable = false)
    private String tipoGama;

    @Column(name = "precio_ensamble", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioEnsamble;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;
}
