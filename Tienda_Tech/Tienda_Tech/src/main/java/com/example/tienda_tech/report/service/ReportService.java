package com.example.tienda_tech.report.service;

import com.example.tienda_tech.report.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    // ===== CARGA DE DATOS =====
    AdminSummaryDTO loadAdminSummary(LocalDate desde, LocalDate hasta);

    List<KardexRow>           loadKardex(LocalDate desde, LocalDate hasta);
    List<UserReportRow>       loadUsers(LocalDate desde, LocalDate hasta);
    List<ProductReportRow>    loadProducts();
    List<OrderReportRow>      loadOrders(LocalDate desde, LocalDate hasta);
    List<LowStockRow>         loadLowStock(int limiteStock);
    List<SalesByProductRow>   loadSalesByProduct(LocalDate desde, LocalDate hasta);
    List<RoleReportRow>       loadRoles();
    List<CityReportRow>       loadCities();
    List<ProvinceReportRow>   loadProvinces();
    List<PaymentMethodRow>    loadPaymentMethods();

    /** Kardex valorizado PEPS por producto (null = todos) y rango (pueden ser null). */
    List<KardexValRow> loadKardexValorizado(LocalDate desde, LocalDate hasta, Integer productoId);

    // ===== PDFs =====
    byte[] buildAdminSummaryPdf(AdminSummaryDTO dto, List<KardexRow> kardex, LocalDate desde, LocalDate hasta);

    byte[] buildMultiReportPdf(AdminSummaryDTO dto,
                               List<UserReportRow> users,
                               List<ProductReportRow> products,
                               List<OrderReportRow> orders,
                               List<LowStockRow> lowStock,
                               List<SalesByProductRow> salesByProduct,
                               List<RoleReportRow> roles,
                               List<CityReportRow> cities,
                               List<ProvinceReportRow> provinces,
                               List<PaymentMethodRow> paymentMethods,
                               List<KardexRow> kardex,
                               LocalDate desde,
                               LocalDate hasta);

    /** PDF con tabla tipo Excel para el Kardex valorizado PEPS. */
    byte[] buildKardexValorizadoPdf(String titulo, List<KardexValRow> rows);
}
