package com.example.tienda_tech.report.controller;

import com.example.tienda_tech.report.dto.*;
import com.example.tienda_tech.report.service.ReportService;
import com.example.tienda_tech.report.util.ReportPdfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    private final ReportService reportService;

    // ===================== helpers =====================
    private static LocalDate parseOr(LocalDate fallback, String raw) {
        return (raw != null && !raw.isBlank()) ? LocalDate.parse(raw) : fallback;
    }
    private static HttpHeaders dl(String fileName){
        HttpHeaders h = new HttpHeaders();
        h.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return h;
    }

    // ===================================================
    // 1) TUS ENDPOINTS EXISTENTES (NO TOCADOS)
    // ===================================================

    // --------------------------------------------------------------------
    // PDF GENERAL (Resumen + todos los datasets)
    // Endpoint: /api/report/admin/pdf?desde=yyyy-MM-dd&hasta=yyyy-MM-dd
    // --------------------------------------------------------------------
    @GetMapping("/admin/pdf")
    public ResponseEntity<byte[]> adminPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDate d2 = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : LocalDate.now();
        LocalDate d1 = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : d2.minusDays(30);

        AdminSummaryDTO summary = reportService.loadAdminSummary(d1, d2);

        // datasets
        List<UserReportRow>         users          = reportService.loadUsers(d1, d2);
        List<ProductReportRow>      products       = reportService.loadProducts();
        List<OrderReportRow>        orders         = reportService.loadOrders(d1, d2);
        List<LowStockRow>           lowStock       = reportService.loadLowStock(5);
        List<SalesByProductRow>     salesByProduct = reportService.loadSalesByProduct(d1, d2);
        List<RoleReportRow>         roles          = reportService.loadRoles();
        List<CityReportRow>         cities         = reportService.loadCities();
        List<ProvinceReportRow>     provinces      = reportService.loadProvinces();
        List<PaymentMethodRow>      paymentMethods = reportService.loadPaymentMethods();
        List<KardexRow>             kardex         = reportService.loadKardex(d1, d2);

        byte[] pdf = reportService.buildMultiReportPdf(
                summary,
                users, products, orders,
                lowStock, salesByProduct, roles,
                cities, provinces, paymentMethods,
                kardex,
                d1, d2
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ReporteGeneral-" + d1 + "_a_" + d2 + ".pdf")
                .body(pdf);
    }

    // --------------------------------------------------------------------
    // PDF KARDEX VALORIZADO (opcional: por producto)
    // Endpoint: /api/report/kardex/pdf?desde=yyyy-MM-dd&hasta=yyyy-MM-dd&productoId=123
    // --------------------------------------------------------------------
    @GetMapping("/kardex/pdf")
    public ResponseEntity<byte[]> kardexPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) Integer productoId
    ) {
        LocalDate d1 = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : LocalDate.of(2000,1,1);
        LocalDate d2 = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : LocalDate.now();

        List<KardexValRow> rows = reportService.loadKardexValorizado(d1, d2, productoId);
        byte[] pdf = reportService.buildKardexValorizadoPdf(
                "Kardex Valorizado - Prod " + (productoId != null ? productoId : "Todos"),
                rows
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=KardexValorizado-" + d1 + "_a_" + d2 +
                                (productoId != null ? ("-P" + productoId) : "") + ".pdf")
                .body(pdf);
    }

    // ===================================================
    // 2) NUEVOS ENDPOINTS PARA EL PANEL DE REPORTES
    // ===================================================

    // —— General (TODOS los datos)
    @GetMapping("/general/pdf")
    public ResponseEntity<byte[]> generalAll(){
        LocalDate d2 = LocalDate.now();
        LocalDate d1 = LocalDate.of(2000,1,1);

        AdminSummaryDTO summary = reportService.loadAdminSummary(d1, d2);
        List<UserReportRow>     users          = reportService.loadUsers(d1, d2);
        List<ProductReportRow>  products       = reportService.loadProducts();
        List<OrderReportRow>    orders         = reportService.loadOrders(d1, d2);
        List<LowStockRow>       lowStock       = reportService.loadLowStock(5);
        List<SalesByProductRow> salesByProduct = reportService.loadSalesByProduct(d1, d2);
        List<RoleReportRow>     roles          = reportService.loadRoles();
        List<CityReportRow>     cities         = reportService.loadCities();
        List<ProvinceReportRow> provinces      = reportService.loadProvinces();
        List<PaymentMethodRow>  payMethods     = reportService.loadPaymentMethods();
        List<KardexRow>         kardex         = reportService.loadKardex(d1, d2);

        byte[] pdf = reportService.buildMultiReportPdf(
                summary, users, products, orders, lowStock,
                salesByProduct, roles, cities, provinces, payMethods,
                kardex, d1, d2
        );
        return ResponseEntity.ok()
                .headers(dl("ReporteGeneral-Todos.pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // —— General (por rango)
    @GetMapping("/general/pdf-range")
    public ResponseEntity<byte[]> generalByDate(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);

        AdminSummaryDTO summary = reportService.loadAdminSummary(d1, d2);
        List<UserReportRow>     users          = reportService.loadUsers(d1, d2);
        List<ProductReportRow>  products       = reportService.loadProducts();
        List<OrderReportRow>    orders         = reportService.loadOrders(d1, d2);
        List<LowStockRow>       lowStock       = reportService.loadLowStock(5);
        List<SalesByProductRow> salesByProduct = reportService.loadSalesByProduct(d1, d2);
        List<RoleReportRow>     roles          = reportService.loadRoles();
        List<CityReportRow>     cities         = reportService.loadCities();
        List<ProvinceReportRow> provinces      = reportService.loadProvinces();
        List<PaymentMethodRow>  payMethods     = reportService.loadPaymentMethods();
        List<KardexRow>         kardex         = reportService.loadKardex(d1, d2);

        byte[] pdf = reportService.buildMultiReportPdf(
                summary, users, products, orders, lowStock,
                salesByProduct, roles, cities, provinces, payMethods,
                kardex, d1, d2
        );
        return ResponseEntity.ok()
                .headers(dl("ReporteGeneral-"+d1+"_a_"+d2+".pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // —— Individuales (cada uno en su propio PDF)
    @GetMapping("/usuarios/pdf")
    public ResponseEntity<byte[]> usersPdf(){
        LocalDate d1 = LocalDate.of(2000,1,1), d2 = LocalDate.now();
        List<UserReportRow> data = reportService.loadUsers(d1, d2);
        byte[] pdf = ReportPdfUtil.buildUsersPdf("Reporte de Usuarios", data);
        return ResponseEntity.ok().headers(dl("Usuarios.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/productos/pdf")
    public ResponseEntity<byte[]> productsPdf(){
        List<ProductReportRow> data = reportService.loadProducts();
        byte[] pdf = ReportPdfUtil.buildProductsPdf("Reporte de Productos", data);
        return ResponseEntity.ok().headers(dl("Productos.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/ordenes/pdf")
    public ResponseEntity<byte[]> ordersPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);
        List<OrderReportRow> data = reportService.loadOrders(d1, d2);
        byte[] pdf = ReportPdfUtil.buildOrdersPdf("Reporte de Órdenes ("+d1+" a "+d2+")", data);
        return ResponseEntity.ok().headers(dl("Ordenes-"+d1+"_a_"+d2+".pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/stock-bajo/pdf")
    public ResponseEntity<byte[]> lowStockPdf(@RequestParam(defaultValue = "5") int umbral){
        List<LowStockRow> data = reportService.loadLowStock(umbral);
        byte[] pdf = ReportPdfUtil.buildLowStockPdf("Stock Bajo (≤ "+umbral+")", data);
        return ResponseEntity.ok().headers(dl("StockBajo-Umbral"+umbral+".pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/ventas-producto/pdf")
    public ResponseEntity<byte[]> salesByProductPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);
        List<SalesByProductRow> data = reportService.loadSalesByProduct(d1, d2);
        byte[] pdf = ReportPdfUtil.buildSalesByProductPdf("Ventas por Producto ("+d1+" a "+d2+")", data);
        return ResponseEntity.ok().headers(dl("VentasPorProducto-"+d1+"_a_"+d2+".pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/roles/pdf")
    public ResponseEntity<byte[]> rolesPdf(){
        List<RoleReportRow> data = reportService.loadRoles();
        byte[] pdf = ReportPdfUtil.buildRolesPdf("Roles y #Usuarios", data);
        return ResponseEntity.ok().headers(dl("Roles.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/ciudades/pdf")
    public ResponseEntity<byte[]> citiesPdf(){
        List<CityReportRow> data = reportService.loadCities();
        byte[] pdf = ReportPdfUtil.buildCitiesPdf("Ciudades", data);
        return ResponseEntity.ok().headers(dl("Ciudades.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/provincias/pdf")
    public ResponseEntity<byte[]> provincesPdf(){
        List<ProvinceReportRow> data = reportService.loadProvinces();
        byte[] pdf = ReportPdfUtil.buildProvincesPdf("Provincias", data);
        return ResponseEntity.ok().headers(dl("Provincias.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/metodos-pago/pdf")
    public ResponseEntity<byte[]> paymentMethodsPdf(){
        List<PaymentMethodRow> data = reportService.loadPaymentMethods();
        byte[] pdf = ReportPdfUtil.buildPaymentMethodsPdf("Métodos de Pago", data);
        return ResponseEntity.ok().headers(dl("MetodosPago.pdf")).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
