package com.example.tienda_tech.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CheckoutResult {
    int ordenId;
    BigDecimal subtotal;
    BigDecimal impuestos;
    BigDecimal total;
    int facturaId;
    String numero;
}