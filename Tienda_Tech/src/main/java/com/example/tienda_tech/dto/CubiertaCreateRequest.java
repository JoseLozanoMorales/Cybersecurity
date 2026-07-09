package com.example.tienda_tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CubiertaCreateRequest {
  @NotBlank private String nombre;
  private String enlace;
  @NotNull private Integer marca_id;
  @NotNull private Integer gama_id;
  @NotNull private Integer iva_id;

  private Long tamanio_gpu;            // mm (longitud GPU)
  private Long tamanio_refrigeracion;  // mm (radiador soportado)

  // getters/setters
  public String getNombre() { return nombre; }
  public void setNombre(String v) { nombre = v; }
  public String getEnlace() { return enlace; }
  public void setEnlace(String v) { enlace = v; }
  public Integer getMarca_id() { return marca_id; }
  public void setMarca_id(Integer v) { marca_id = v; }
  public Integer getGama_id() { return gama_id; }
  public void setGama_id(Integer v) { gama_id = v; }
  public Integer getIva_id() { return iva_id; }
  public void setIva_id(Integer v) { iva_id = v; }
  public Long getTamanio_gpu() { return tamanio_gpu; }
  public void setTamanio_gpu(Long v) { tamanio_gpu = v; }
  public Long getTamanio_refrigeracion() { return tamanio_refrigeracion; }
  public void setTamanio_refrigeracion(Long v) { tamanio_refrigeracion = v; }
}
