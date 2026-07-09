package com.example.tienda_tech.model;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity @Immutable
@Table(name = "v_sugerencia_build_header", schema = "public")
public class VBuildHeader {
    @Id @Column(name = "sugerencia_id") private Integer sugerenciaId;
    private String tipo;
    @Column(name = "presupuesto_total") private BigDecimal presupuestoTotal;
    @Column(name = "total_precio")      private BigDecimal totalPrecio;
    @Column(name = "total_componentes") private Integer totalComponentes;

    public Integer getSugerenciaId() { return sugerenciaId; }
    public String getTipo() { return tipo; }
    public BigDecimal getPresupuestoTotal() { return presupuestoTotal; }
    public BigDecimal getTotalPrecio() { return totalPrecio; }
    public Integer getTotalComponentes() { return totalComponentes; }
}
