package com.example.tienda_tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MotherboardCreateRequest {
  @NotBlank private String nombre;
  private String enlace;
  @NotNull private Integer marca_id;
  @NotNull private Integer gama_id;
  @NotNull private Integer iva_id;

  private String socket;        // p.ej. "LGA 1700", "AM5"
  private Long   velocidad_ram; // MHz (p.ej. 6000)
  private String chipset;       // p.ej. "B760", "Z790", "B650"

  // getters/setters
  public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
  public String getEnlace(){return enlace;} public void setEnlace(String v){enlace=v;}
  public Integer getMarca_id(){return marca_id;} public void setMarca_id(Integer v){marca_id=v;}
  public Integer getGama_id(){return gama_id;} public void setGama_id(Integer v){gama_id=v;}
  public Integer getIva_id(){return iva_id;} public void setIva_id(Integer v){iva_id=v;}
  public String getSocket(){return socket;} public void setSocket(String v){socket=v;}
  public Long getVelocidad_ram(){return velocidad_ram;} public void setVelocidad_ram(Long v){velocidad_ram=v;}
  public String getChipset(){return chipset;} public void setChipset(String v){chipset=v;}
}
