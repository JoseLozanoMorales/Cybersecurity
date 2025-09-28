package com.example.tienda_tech.dto.galeria;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaleriaDTO {
    private Long    galeriaId;
    private boolean portada;
    private String  mimeType;
    private Integer ancho;
    private Integer alto;
    private Long    pesoBytes;

    private String  url;      // /api/galeria/{id}/raw
    private String  thumbUrl; // /api/galeria/{id}/thumb
}
