// src/main/java/com/example/tienda_tech/model/GaleriaProducto.java
package com.example.tienda_tech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "galeria_productos")
public class GaleriaProducto {

    @Id
    @Column(name = "galeria_id", nullable = false)
    private Integer id;  // <-- INTEGER, no Long

    @OneToOne(mappedBy = "galeria", fetch = FetchType.LAZY)
    private GaleriaMedia media;

    // ...otros campos de la galería...

    // getters/setters
}
