package com.example.tienda_tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FuenteCreateRequest {
  @NotBlank private String nombre;
  private String enlace;
  @NotNull private Integer marca_id;
  @NotNull private Integer gama_id;
  @NotNull private Integer iva_id;

  // del front vendrá en ASCII, lo mapeamos en el service
  private Long consumo_energia; // Watts

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
  public Long getConsumo_energia() { return consumo_energia; }
  public void setConsumo_energia(Long v) { consumo_energia = v; }
}
