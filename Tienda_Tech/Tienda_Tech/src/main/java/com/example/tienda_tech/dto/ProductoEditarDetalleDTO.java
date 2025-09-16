package com.example.tienda_tech.dto;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductoEditarDetalleDTO {
    @JsonProperty("producto_id")   private Integer productoId;
    private String nombre;
    private String enlace;
    @JsonProperty("iva_id")        private Integer ivaId;
    private Boolean habilitado;
    @JsonProperty("precio_unitario") private BigDecimal precioUnitario;
    @JsonProperty("costo_actual")    private BigDecimal costoActual;
    @JsonProperty("categoria_id")    private Integer categoriaId;

    // getters/setters (genéralos con tu IDE)
    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer v) { this.productoId = v; }
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
    public BigDecimal getCostoActual() { return costoActual; }
    public void setCostoActual(BigDecimal v) { this.costoActual = v; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer v) { this.categoriaId = v; }
}
