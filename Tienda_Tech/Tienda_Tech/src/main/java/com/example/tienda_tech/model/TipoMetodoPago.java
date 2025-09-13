package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_metodopago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "tipoId")
public class TipoMetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_id")
    private Integer tipoId;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "habilitado")
    @Builder.Default
    private Boolean habilitado = true;
}
