package com.example.tienda_tech.report.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentMethodRow {
    private Integer metodopagoId; private String tipo; private Integer usuarioId; private Boolean habilitado;
}
