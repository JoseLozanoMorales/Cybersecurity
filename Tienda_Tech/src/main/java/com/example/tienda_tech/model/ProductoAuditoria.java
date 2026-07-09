package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto_auditoria")
public class ProductoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL/sequence por defecto en Postgres
    @Column(name = "auditoria_id")
    private Long auditoriaId;

    @Column(name = "producto_id")
    private Integer productoId;

    @Column(name = "nombre", length = 150)
    private String nombre;

    @Column(name = "preciounitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "enlace", length = 255)
    private String enlace;

    @Column(name = "fecha")
    private LocalDate fecha; // DATE

    @Column(name = "stock")
    private Short stock; // SMALLINT

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

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "valor_inventario", precision = 18, scale = 2)
    private BigDecimal valorInventario;

    // --- Campos "después" ---
    @Column(name = "preciounitario_despues", precision = 10, scale = 2)
    private BigDecimal precioUnitarioDespues;

    @Column(name = "stock_despues")
    private Short stockDespues;

    @Column(name = "iva_id_despues")
    private Integer ivaIdDespues;

    @Column(name = "habilitado_despues")
    private Boolean habilitadoDespues;

    @Column(name = "fechahorareg", insertable = false, updatable = false)
    private LocalDateTime fechaHoraReg; // TIMESTAMP WITHOUT TIME ZONE (DEFAULT now())

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "tipo", length = 1)
    private String tipo; // CHAR(1)

    @Column(name = "nombre_despues", length = 150)
    private String nombreDespues;
}
