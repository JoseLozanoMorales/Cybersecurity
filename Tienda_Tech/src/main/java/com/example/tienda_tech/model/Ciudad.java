// src/main/java/com/example/tienda_tech/model/Ciudad.java
package com.example.tienda_tech.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ciudad")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "direcciones")
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ciudad_id_seq")
    @SequenceGenerator(name = "ciudad_id_seq", sequenceName = "ciudad_ciudad_id_seq", allocationSize = 1)
    @Column(name = "ciudad_id")
    private Short ciudadId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "provincia_id", nullable = false)
    private Short provinciaId;

    @JsonIgnore
    @OneToMany(mappedBy = "ciudad", fetch = FetchType.LAZY)
    private List<Direccion> direcciones;
}
