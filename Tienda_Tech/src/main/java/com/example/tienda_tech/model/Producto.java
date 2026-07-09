// === Archivo: src/main/java/com/example/tienda_tech/model/Producto.java
package com.example.tienda_tech.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "producto") // <-- si tu tabla se llama distinto, cámbialo aquí
public class Producto {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "producto_id")
private Integer productoId;


@Column(name = "nombre", length = 150, nullable = false)
private String nombre;


@Column(name = "preciounitario", precision = 10, scale = 2, nullable = false)
private BigDecimal precioUnitario;


@Column(name = "enlace", length = 255)
private String enlace;


@Column(name = "fecha")
private LocalDate fecha; // date


@Column(name = "stock")
private Short stock; // smallint -> Integer/Short


@Column(name = "marca_id")
private Integer marcaId;


@Column(name = "gama_id")
private Integer gamaId;


@Column(name = "iva_id")
private Integer ivaId;


@Column(name = "costo", precision = 10, scale = 2)
private BigDecimal costo;


@Column(name = "habilitado")
private Boolean habilitado;
}