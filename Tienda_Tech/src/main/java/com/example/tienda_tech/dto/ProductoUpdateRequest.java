package com.example.tienda_tech.dto;

public class ProductoUpdateRequest {

  // --- Comunes (opcionales) ---
  private String  nombre;
  private String  enlace;
  private Integer marca_id;
  private Integer gama_id;
  private Integer iva_id;
  private String  fecha;       // ISO yyyy-MM-dd (opcional)
  private Boolean habilitado;

  // --- Almacenamiento ---
  private Long   capacidad;
  private String tipo;
  private String capacidad_unidad; // "GB" | "TB"

  // --- CPU ---
  private String sockets;
  private Short  generacion;

  // --- CPU Cooler ---
  private Long   tamanio;
  private String socket;

  // --- Cubierta ---
  private Long tamanio_gpu;
  private Long tamanio_refrigeracion;

  // --- Fuente de poder / GPU ---
  private Long consumo_energia; // W

  // --- RAM ---
  private Long velocidades;

  // --- Motherboard ---
  private Long   velocidad_ram;
  private String chipset;

  // ====== GETTERS / SETTERS ======

  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }

  public String getEnlace() { return enlace; }
  public void setEnlace(String enlace) { this.enlace = enlace; }

  public Integer getMarca_id() { return marca_id; }
  public void setMarca_id(Integer marca_id) { this.marca_id = marca_id; }

  public Integer getGama_id() { return gama_id; }
  public void setGama_id(Integer gama_id) { this.gama_id = gama_id; }

  public Integer getIva_id() { return iva_id; }
  public void setIva_id(Integer iva_id) { this.iva_id = iva_id; }

  public String getFecha() { return fecha; }
  public void setFecha(String fecha) { this.fecha = fecha; }

  public Boolean getHabilitado() { return habilitado; }
  public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }

  public Long getCapacidad() { return capacidad; }
  public void setCapacidad(Long capacidad) { this.capacidad = capacidad; }

  public String getTipo() { return tipo; }
  public void setTipo(String tipo) { this.tipo = tipo; }

  public String getCapacidad_unidad() { return capacidad_unidad; }
  public void setCapacidad_unidad(String capacidad_unidad) { this.capacidad_unidad = capacidad_unidad; }

  public String getSockets() { return sockets; }
  public void setSockets(String sockets) { this.sockets = sockets; }

  public Short getGeneracion() { return generacion; }
  public void setGeneracion(Short generacion) { this.generacion = generacion; }

  public Long getTamanio() { return tamanio; }
  public void setTamanio(Long tamanio) { this.tamanio = tamanio; }

  public String getSocket() { return socket; }
  public void setSocket(String socket) { this.socket = socket; }

  public Long getTamanio_gpu() { return tamanio_gpu; }
  public void setTamanio_gpu(Long tamanio_gpu) { this.tamanio_gpu = tamanio_gpu; }

  public Long getTamanio_refrigeracion() { return tamanio_refrigeracion; }
  public void setTamanio_refrigeracion(Long tamanio_refrigeracion) { this.tamanio_refrigeracion = tamanio_refrigeracion; }

  public Long getConsumo_energia() { return consumo_energia; }
  public void setConsumo_energia(Long consumo_energia) { this.consumo_energia = consumo_energia; }

  public Long getVelocidades() { return velocidades; }
  public void setVelocidades(Long velocidades) { this.velocidades = velocidades; }

  public Long getVelocidad_ram() { return velocidad_ram; }
  public void setVelocidad_ram(Long velocidad_ram) { this.velocidad_ram = velocidad_ram; }

  public String getChipset() { return chipset; }
  public void setChipset(String chipset) { this.chipset = chipset; }
}
