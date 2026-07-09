package com.example.tienda_tech.report.service;

import com.example.tienda_tech.report.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    AdminSummaryDTO loadAdminSummary(LocalDate desde, LocalDate hasta);

    List<UserReportRow>     loadUsers(LocalDate d1, LocalDate d2);
    List<ProductReportRow>  loadProducts();
    List<OrderReportRow>    loadOrders(LocalDate d1, LocalDate d2);
    List<LowStockRow>       loadLowStock(int limiteStock);
    List<SalesByProductRow> loadSalesByProduct(LocalDate d1, LocalDate d2);
    List<RoleReportRow>     loadRoles();
    List<CityReportRow>     loadCities();
    List<ProvinceReportRow> loadProvinces();
    List<PaymentMethodRow>  loadPaymentMethods();

    // Solo Kardex valorizado
    List<KardexValRow> loadKardexValorizado(LocalDate desde, LocalDate hasta, Integer productoId);

    // PDFs
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
                               List<KardexRow> kardexIgnorado,
                               LocalDate desde, LocalDate hasta);
    byte[] buildKardexValorizadoPdf(String titulo, List<KardexValRow> rows);
}
