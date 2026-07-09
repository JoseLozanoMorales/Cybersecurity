package com.example.tienda_tech.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KardexRow {
    private LocalDateTime fecha;

    // Campos para kardex simple (reportes)
    private Integer productoId;
    private String producto;
    private String movimiento;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    // Campos para kardex valorizado (entradas/salidas/saldos)
    private String detalle;
    private BigDecimal entradasCant;
    private BigDecimal entradasCt;
    private BigDecimal salidasCant;
    private BigDecimal salidasCt;
    private BigDecimal saldoCant;
    private BigDecimal saldoCt;

    public KardexRow() {}

    public KardexRow(LocalDateTime fecha,
                     Integer productoId, String producto, String movimiento, Integer cantidad,
                     BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal iva, BigDecimal total,
                     String detalle,
                     BigDecimal entradasCant, BigDecimal entradasCt,
                     BigDecimal salidasCant, BigDecimal salidasCt,
                     BigDecimal saldoCant, BigDecimal saldoCt) {
        this.fecha = fecha;
        this.productoId = productoId;
        this.producto = producto;
        this.movimiento = movimiento;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.detalle = detalle;
        this.entradasCant = entradasCant;
        this.entradasCt = entradasCt;
        this.salidasCant = salidasCant;
        this.salidasCt = salidasCt;
        this.saldoCant = saldoCant;
        this.saldoCt = saldoCt;
    }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private LocalDateTime fecha;
        private Integer productoId;
        private String producto;
        private String movimiento;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private BigDecimal iva;
        private BigDecimal total;
        private String detalle;
        private BigDecimal entradasCant;
        private BigDecimal entradasCt;
        private BigDecimal salidasCant;
        private BigDecimal salidasCt;
        private BigDecimal saldoCant;
        private BigDecimal saldoCt;

        public Builder fecha(LocalDateTime v){ this.fecha = v; return this; }
        public Builder productoId(Integer v){ this.productoId = v; return this; }
        public Builder producto(String v){ this.producto = v; return this; }
        public Builder movimiento(String v){ this.movimiento = v; return this; }
        public Builder cantidad(Integer v){ this.cantidad = v; return this; }
        public Builder precioUnitario(BigDecimal v){ this.precioUnitario = v; return this; }
        public Builder subtotal(BigDecimal v){ this.subtotal = v; return this; }
        public Builder iva(BigDecimal v){ this.iva = v; return this; }
        public Builder total(BigDecimal v){ this.total = v; return this; }

        public Builder detalle(String v){ this.detalle = v; return this; }
        public Builder entradasCant(BigDecimal v){ this.entradasCant = v; return this; }
        public Builder entradasCt(BigDecimal v){ this.entradasCt = v; return this; }
        public Builder salidasCant(BigDecimal v){ this.salidasCant = v; return this; }
        public Builder salidasCt(BigDecimal v){ this.salidasCt = v; return this; }
        public Builder saldoCant(BigDecimal v){ this.saldoCant = v; return this; }
        public Builder saldoCt(BigDecimal v){ this.saldoCt = v; return this; }

        public KardexRow build() {
            return new KardexRow(fecha,
                    productoId, producto, movimiento, cantidad,
                    precioUnitario, subtotal, iva, total,
                    detalle,
                    entradasCant, entradasCt,
                    salidasCant, salidasCt,
                    saldoCant, saldoCt);
        }
    }

    // Getters
    public LocalDateTime getFecha() { return fecha; }
    public Integer getProductoId() { return productoId; }
    public String getProducto() { return producto; }
    public String getMovimiento() { return movimiento; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getIva() { return iva; }
    public BigDecimal getTotal() { return total; }
    public String getDetalle() { return detalle; }
    public BigDecimal getEntradasCant() { return entradasCant; }
    public BigDecimal getEntradasCt() { return entradasCt; }
    public BigDecimal getSalidasCant() { return salidasCant; }
    public BigDecimal getSalidasCt() { return salidasCt; }
    public BigDecimal getSaldoCant() { return saldoCant; }
    public BigDecimal getSaldoCt() { return saldoCt; }

    // Setters
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }
    public void setProducto(String producto) { this.producto = producto; }
    public void setMovimiento(String movimiento) { this.movimiento = movimiento; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public void setEntradasCant(BigDecimal entradasCant) { this.entradasCant = entradasCant; }
    public void setEntradasCt(BigDecimal entradasCt) { this.entradasCt = entradasCt; }
    public void setSalidasCant(BigDecimal salidasCant) { this.salidasCant = salidasCant; }
    public void setSalidasCt(BigDecimal salidasCt) { this.salidasCt = salidasCt; }
    public void setSaldoCant(BigDecimal saldoCant) { this.saldoCant = saldoCant; }
    public void setSaldoCt(BigDecimal saldoCt) { this.saldoCt = saldoCt; }
}
