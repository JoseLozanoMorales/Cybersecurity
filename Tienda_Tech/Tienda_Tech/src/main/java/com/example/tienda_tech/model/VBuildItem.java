package com.example.tienda_tech.model;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity @Immutable
@Table(name = "v_sugerencia_build_items", schema = "public")
@IdClass(VBuildItemId.class)
public class VBuildItem {
    @Id @Column(name = "sugerencia_id") private Integer sugerenciaId;
    @Id private String categoria;

    private String producto;
    @Column(name = "precio_snap") private Double precio;
    private Double score;
    @Column(name = "compat_ok")   private Boolean compatOk;
    private String motivo;

    public Integer getSugerenciaId() { return sugerenciaId; }
    public String getCategoria() { return categoria; }
    public String getProducto() { return producto; }
    public Double getPrecio() { return precio; }
    public Double getScore() { return score; }
    public Boolean getCompatOk() { return compatOk; }
    public String getMotivo() { return motivo; }
}
