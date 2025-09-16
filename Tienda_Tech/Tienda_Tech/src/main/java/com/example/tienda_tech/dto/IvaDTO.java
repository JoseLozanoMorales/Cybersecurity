package com.example.tienda_tech.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IvaDTO {
    @JsonProperty("iva_id")  private Integer ivaId;
    private BigDecimal porcentaje;
    private Boolean habilitado;
    private String etiqueta;

    public Integer getIvaId() { return ivaId; }
    public void setIvaId(Integer v) { this.ivaId = v; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal v) { this.porcentaje = v; }
    public Boolean getHabilitado() { return habilitado; }
    public void setHabilitado(Boolean v) { this.habilitado = v; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String v) { this.etiqueta = v; }
}
