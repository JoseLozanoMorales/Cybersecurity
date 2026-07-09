package com.example.tienda_tech.report.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminSummaryDTO {
    private long totalUsuarios;
    private long totalProductos;
    private long totalOrdenes;
    private long totalDetallesOrden;
    private long totalMovimientosInventario; // si no hay tabla dedicada, dejar 0
    private double totalVentas;
}
