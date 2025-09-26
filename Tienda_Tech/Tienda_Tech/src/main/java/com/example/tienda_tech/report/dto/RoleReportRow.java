package com.example.tienda_tech.report.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleReportRow {
    private Integer rolId; private String nombre; private Long usuarios;
}
