// src/main/java/com/example/tienda_tech/model/CategoriaProducto.java
package com.example.tienda_tech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_producto")
public class CategoriaProducto {
  @Id
  @Column(name = "id_categoria")
  private Integer idCategoria;

  private String nombre;

  private Boolean habilitado;

  // getters/setters
  public Integer getIdCategoria() { return idCategoria; }
  public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public Boolean getHabilitado() { return habilitado; }
  public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }
}
