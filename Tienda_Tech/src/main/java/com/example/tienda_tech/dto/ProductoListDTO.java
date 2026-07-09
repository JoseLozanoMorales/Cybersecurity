package com.example.tienda_tech.dto;


import com.example.tienda_tech.model.Producto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoListDTO {
private Integer producto_id;
private String nombre;
private BigDecimal preciounitario;
private String enlace;
private LocalDate fecha;
private Short stock;
private Integer marca_id;
private Integer gama_id;
private Integer iva_id;
private BigDecimal costo;
private Boolean habilitado;


public static ProductoListDTO fromEntity(Producto p){
return ProductoListDTO.builder()
.producto_id(p.getProductoId())
.nombre(p.getNombre())
.preciounitario(p.getPrecioUnitario())
.enlace(p.getEnlace())
.fecha(p.getFecha())
.stock(p.getStock())
.marca_id(p.getMarcaId())
.gama_id(p.getGamaId())
.iva_id(p.getIvaId())
.costo(p.getCosto())
.habilitado(p.getHabilitado())
.build();
}
}