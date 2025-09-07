package com.example.tienda_tech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CpuCoolerCreateRequest {
  @NotBlank private String nombre;
  private String enlace;
  @NotNull private Integer marca_id;
  @NotNull private Integer gama_id;
  @NotNull private Integer iva_id;
  @Min(1) private Long tamanio;     // ej. 120 (mm) o altura en mm según tu UI
  @NotBlank private String socket;  // ej. "LGA 1700"

  // getters/setters
  public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
  public String getEnlace(){return enlace;} public void setEnlace(String v){enlace=v;}
  public Integer getMarca_id(){return marca_id;} public void setMarca_id(Integer v){marca_id=v;}
  public Integer getGama_id(){return gama_id;} public void setGama_id(Integer v){gama_id=v;}
  public Integer getIva_id(){return iva_id;} public void setIva_id(Integer v){iva_id=v;}
  public Long getTamanio(){return tamanio;} public void setTamanio(Long v){tamanio=v;}
  public String getSocket(){return socket;} public void setSocket(String v){socket=v;}
}
