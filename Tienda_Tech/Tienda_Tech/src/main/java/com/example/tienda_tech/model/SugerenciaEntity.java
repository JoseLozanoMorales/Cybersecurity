package com.example.tienda_tech.model;
import jakarta.persistence.*;

@Entity
@Table(name = "sugerencia", schema = "public")
public class SugerenciaEntity {
    @Id
    @Column(name = "sugerencia_id")
    private Integer sugerenciaId;

    // no necesitamos más campos para el @Procedure
    public Integer getSugerenciaId() { return sugerenciaId; }
    public void setSugerenciaId(Integer id) { this.sugerenciaId = id; }
}
