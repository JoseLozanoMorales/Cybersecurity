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
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080" })
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfUtil pdfUtil;   // inyección de bean

    private static LocalDate parseOr(LocalDate fb, String raw){
        return (raw != null && !raw.isBlank()) ? LocalDate.parse(raw) : fb;
    }

    private static HttpHeaders cd(String fileName, boolean inline){
        HttpHeaders h = new HttpHeaders();
        h.add(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=" + fileName);
        h.add("X-Frame-Options", "SAMEORIGIN");
        h.add("Content-Security-Policy",
                "frame-ancestors 'self' http://localhost:3000 http://127.0.0.1:3000 http://localhost:8080");
        h.add("Access-Control-Expose-Headers", "Content-Disposition");
        return h;
    }

    // ===================== GENERAL =====================
    @GetMapping(value="/admin/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> adminPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "false") boolean inline
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);

        AdminSummaryDTO summary = reportService.loadAdminSummary(d1, d2);
        List<UserReportRow>         users          = reportService.loadUsers(d1, d2);
        List<ProductReportRow>      products       = reportService.loadProducts();
        List<OrderReportRow>        orders         = reportService.loadOrders(d1, d2);
        List<LowStockRow>           lowStock       = reportService.loadLowStock(5);
        List<SalesByProductRow>     salesByProduct = reportService.loadSalesByProduct(d1, d2);
        List<RoleReportRow>         roles          = reportService.loadRoles();
        List<CityReportRow>         cities         = reportService.loadCities();
        List<ProvinceReportRow>     provinces      = reportService.loadProvinces();
        List<PaymentMethodRow>      paymentMethods = reportService.loadPaymentMethods();

        byte[] pdf = reportService.buildMultiReportPdf(
                summary, users, products, orders, lowStock,
                salesByProduct, roles, cities, provinces, paymentMethods,
                List.of(), d1, d2
        );
        return ResponseEntity.ok()
                .headers(cd("ReporteGeneral-" + d1 + "_a_" + d2 + ".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value="/general/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generalAll(@RequestParam(defaultValue = "false") boolean inline){
        LocalDate d2 = LocalDate.now(), d1 = LocalDate.of(2000,1,1);

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

        byte[] pdf = reportService.buildMultiReportPdf(
                summary, users, products, orders, lowStock,
                salesByProduct, roles, cities, provinces, payMethods,
                List.of(), d1, d2
        );
        return ResponseEntity.ok()
                .headers(cd("ReporteGeneral-Todos.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value="/general/pdf-range", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generalByDate(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "false") boolean inline
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

        byte[] pdf = reportService.buildMultiReportPdf(
                summary, users, products, orders, lowStock,
                salesByProduct, roles, cities, provinces, payMethods,
                List.of(), d1, d2
        );
        return ResponseEntity.ok()
                .headers(cd("ReporteGeneral-"+d1+"_a_"+d2+".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ===================== KARDEX VALORIZADO =====================
    @GetMapping(value="/kardex/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> kardexPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false, defaultValue = "false") boolean inline
    ){
        LocalDate d1 = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : LocalDate.of(2000,1,1);
        LocalDate d2 = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : LocalDate.now();

        List<KardexValRow> rows = reportService.loadKardexValorizado(d1, d2, productoId);
        byte[] pdf = reportService.buildKardexValorizadoPdf(
                "Kardex Valorizado - Prod " + (productoId != null ? productoId : "Todos"),
                rows
        );
        return ResponseEntity.ok()
                .headers(cd("KardexValorizado-"+d1+"_a_"+d2+(productoId!=null?("-P"+productoId):"")+".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ===================== INDIVIDUALES =====================
    @GetMapping(value="/usuarios/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> usersPdf(@RequestParam(defaultValue = "false") boolean inline){
        LocalDate d1 = LocalDate.of(2000,1,1), d2 = LocalDate.now();
        List<UserReportRow> data = reportService.loadUsers(d1, d2);
        byte[] pdf = pdfUtil.buildUsersPdf("Reporte de Usuarios", data);
        return ResponseEntity.ok().headers(cd("Usuarios.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/productos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> productsPdf(@RequestParam(defaultValue = "false") boolean inline){
        List<ProductReportRow> data = reportService.loadProducts();
        byte[] pdf = pdfUtil.buildProductsPdf("Reporte de Productos", data);
        return ResponseEntity.ok().headers(cd("Productos.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/ordenes/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> ordersPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "false") boolean inline
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);
        List<OrderReportRow> data = reportService.loadOrders(d1, d2);
        byte[] pdf = pdfUtil.buildOrdersPdf("Reporte de Órdenes ("+d1+" a "+d2+")", data);
        return ResponseEntity.ok().headers(cd("Ordenes-"+d1+"_a_"+d2+".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/stock-bajo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> lowStockPdf(
            @RequestParam(defaultValue = "5") int umbral,
            @RequestParam(required = false, defaultValue = "false") boolean inline
    ){
        List<LowStockRow> data = reportService.loadLowStock(umbral);
        byte[] pdf = pdfUtil.buildLowStockPdf("Stock Bajo (≤ "+umbral+")", data);
        return ResponseEntity.ok().headers(cd("StockBajo-Umbral"+umbral+".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/ventas-producto/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> salesByProductPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "false") boolean inline
    ){
        LocalDate d2 = parseOr(LocalDate.now(), hasta);
        LocalDate d1 = parseOr(d2.minusDays(30), desde);
        List<SalesByProductRow> data = reportService.loadSalesByProduct(d1, d2);
        byte[] pdf = pdfUtil.buildSalesByProductPdf("Ventas por Producto ("+d1+" a "+d2+")", data);
        return ResponseEntity.ok().headers(cd("VentasPorProducto-"+d1+"_a_"+d2+".pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/roles/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rolesPdf(@RequestParam(defaultValue = "false") boolean inline){
        List<RoleReportRow> data = reportService.loadRoles();
        byte[] pdf = pdfUtil.buildRolesPdf("Roles y #Usuarios", data);
        return ResponseEntity.ok().headers(cd("Roles.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/ciudades/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> citiesPdf(@RequestParam(defaultValue = "false") boolean inline){
        List<CityReportRow> data = reportService.loadCities();
        byte[] pdf = pdfUtil.buildCitiesPdf("Ciudades", data);
        return ResponseEntity.ok().headers(cd("Ciudades.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/provincias/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> provincesPdf(@RequestParam(defaultValue = "false") boolean inline){
        List<ProvinceReportRow> data = reportService.loadProvinces();
        byte[] pdf = pdfUtil.buildProvincesPdf("Provincias", data);
        return ResponseEntity.ok().headers(cd("Provincias.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping(value="/metodos-pago/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> paymentMethodsPdf(@RequestParam(defaultValue = "false") boolean inline){
        List<PaymentMethodRow> data = reportService.loadPaymentMethods();
        byte[] pdf = pdfUtil.buildPaymentMethodsPdf("Métodos de Pago", data);
        return ResponseEntity.ok().headers(cd("MetodosPago.pdf", inline))
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
