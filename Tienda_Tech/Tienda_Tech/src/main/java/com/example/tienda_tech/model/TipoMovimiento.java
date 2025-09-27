package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipo_movimiento")
public class TipoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_id", nullable = false)
    private Integer tipoId;

    @Column(name = "nombre", length = 20, nullable = false)
    private String nombre;
}
