package com.example.tienda_tech.report.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CityReportRow {
    private Integer ciudadId; private String ciudad; private Integer provinciaId; private String provincia;
}
