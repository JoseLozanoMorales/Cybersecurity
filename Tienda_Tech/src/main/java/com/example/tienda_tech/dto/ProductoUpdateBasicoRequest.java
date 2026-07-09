package com.example.tienda_tech.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductoUpdateBasicoRequest {
    private String  nombre;
    private String  enlace;
    @JsonProperty("iva_id")       private Integer ivaId;
    private Boolean habilitado;
    // El SP espera "preciounitario"
    @JsonProperty("preciounitario") private BigDecimal precioUnitario;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String v) { this.enlace = v; }
    public Integer getIvaId() { return ivaId; }
    public void setIvaId(Integer v) { this.ivaId = v; }
    public Boolean getHabilitado() { return habilitado; }
    public void setHabilitado(Boolean v) { this.habilitado = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }
}
