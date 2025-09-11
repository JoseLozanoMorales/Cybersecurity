package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "galeria_productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Galeria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "galeria_id")
  private Integer galeriaId;   // <-- antes Long

  @Column(name = "producto_id", nullable = false)
  private Integer productoId;

  @Column(name = "descripcion")
  private String descripcion;

  @Column(name = "habilitado")
  private Boolean habilitado = Boolean.TRUE;

  @Column(name = "es_portada")
  private Boolean esPortada = Boolean.FALSE;

  @Column(name = "para_galeria")
  private Boolean paraGaleria = Boolean.TRUE;

  @Column(name = "para_menu")
  private Boolean paraMenu = Boolean.FALSE;

  @Column(name = "posicion_galeria")
  private Integer posicionGaleria;

  @Column(name = "posicion_menu")
  private Integer posicionMenu;

  @Column(name = "mime_type")
  private String mimeType;

  @Column(name = "peso_bytes")
  private Long pesoBytes;

  @Column(name = "ancho")
  private Integer ancho;

  @Column(name = "alto")
  private Integer alto;

  @Column(name = "hash_sha256")
  private byte[] hashSha256;

  // relación 1–1 con el binario
  @OneToOne(mappedBy = "galeria", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
  private GaleriaMedia media;

  /* --- Helpers para que compile tu controller actual --- */
  @Transient public byte[] getContenido() { return media != null ? media.getContenido() : null; }

  public void attachMedia(byte[] bytes){
    if (media == null) media = new GaleriaMedia(this);
    media.setContenido(bytes);
    this.pesoBytes = (bytes != null) ? (long) bytes.length : null;
  }
}
