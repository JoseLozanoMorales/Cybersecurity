package com.example.tienda_tech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "galeria_productos_v2", schema = "public")
public class GaleriaProducto {

    @Id
    @Column(name = "galeria_id")
    private Long galeriaId;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido")
    private byte[] contenido;

    // getters/setters
    public Long getGaleriaId() { return galeriaId; }
    public void setGaleriaId(Long galeriaId) { this.galeriaId = galeriaId; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }
}
