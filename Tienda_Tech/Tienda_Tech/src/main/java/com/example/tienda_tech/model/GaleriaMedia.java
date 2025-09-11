package com.example.tienda_tech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "galeria_media")
@Getter @Setter @NoArgsConstructor
public class GaleriaMedia {

  @Id
  @Column(name = "galeria_id", nullable = false)
  private Integer id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "galeria_id",
              foreignKey = @ForeignKey(name = "galeria_media_galeria_id_fkey"))
  private Galeria galeria;

  @Lob
  @Column(name = "contenido", nullable = false, columnDefinition = "bytea")
  private byte[] contenido;

  public GaleriaMedia(Galeria galeria) {
    this.galeria = galeria;
    this.id = (galeria != null) ? galeria.getGaleriaId() : null;
  }
}
