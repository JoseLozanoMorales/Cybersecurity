package com.example.tienda_tech.dto;

import lombok.Data;

@Data
public class CarritoAddReq {
    private Integer productoId;
    private Integer cantidad;
}