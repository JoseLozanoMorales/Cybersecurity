package com.example.tienda_tech.model;
import java.io.Serializable;
import java.util.Objects;

public class VBuildItemId implements Serializable {
    private Integer sugerenciaId;
    private String categoria;

    public VBuildItemId() {}
    public VBuildItemId(Integer sugerenciaId, String categoria) {
        this.sugerenciaId = sugerenciaId; this.categoria = categoria;
    }
    @Override public boolean equals(Object o){
        if (this==o) return true;
        if (!(o instanceof VBuildItemId v)) return false;
        return Objects.equals(sugerenciaId, v.sugerenciaId) &&
                Objects.equals(categoria, v.categoria);
    }
    @Override public int hashCode(){ return Objects.hash(sugerenciaId, categoria); }
}
