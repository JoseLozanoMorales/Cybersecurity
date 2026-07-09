package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id", nullable = false)
    private Integer rolId;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Builder.Default
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
