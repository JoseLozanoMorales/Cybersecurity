package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "factura_encabezado")public class FacturaEncabezado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factura_id", nullable = false)
    private Integer facturaId;

    @Column(name = "orden_id", nullable = false)
    private Integer ordenId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "fechaemision")
    private LocalDate fechaEmision;

    @Column(name = "cedula", length = 10)
    private String cedula;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "telefono", length = 10)
    private String telefono;

    @Column(name = "direccionentrega", length = 355)
    private String direccionEntrega;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "numero", length = 20)
    private String numero;
}
