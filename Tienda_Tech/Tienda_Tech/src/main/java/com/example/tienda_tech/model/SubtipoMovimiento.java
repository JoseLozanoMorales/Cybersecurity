package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subtipo_movimiento")
public class SubtipoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subtipo_id", nullable = false)
    private Integer subtipoId;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "tipo_id", nullable = false)
    private Integer tipoId;
}
