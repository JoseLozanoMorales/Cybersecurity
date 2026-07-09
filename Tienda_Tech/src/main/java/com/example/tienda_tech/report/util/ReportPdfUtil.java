package com.example.tienda_tech.report.util;

import com.example.tienda_tech.report.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class ReportPdfUtil {

    // ==== Estilos ====
    private static final Font H1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font TD = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color DARK = new Color(55, 55, 55);
    private static final DecimalFormat DF2 = new DecimalFormat("#,##0.##");
    private static final DecimalFormat DF0 = new DecimalFormat("#,##0");

    private static String fmt(BigDecimal v){ return v==null ? "" : DF2.format(v); }
    private static String fmt(Integer v){ return v==null ? "" : DF0.format(v); }
    private static String fmt(Long v){ return v==null ? "" : DF0.format(v); }
    private static String fdt(LocalDateTime dt){ return dt==null ? "" : dt.toString().replace('T',' '); }
    private static String fdd(LocalDate d){ return d==null ? "" : d.toString(); }

    private static PdfPCell th(String txt){
        PdfPCell c = new PdfPCell(new Phrase(txt, TH));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(DARK);
        c.setPadding(5f);
        return c;
    }
    private static PdfPCell td(String txt){
        PdfPCell c = new PdfPCell(new Phrase(txt==null?"":txt, TD));
        c.setPadding(4f);
        return c;
    }
    private static PdfPCell tdRight(String txt){
        PdfPCell c = td(txt);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private static ByteArrayOutputStream start(Document doc) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();
        return out;
    }
    private static void addTitle(Document doc, String title) throws Exception {
        Paragraph p = new Paragraph(title, H1);
        p.setSpacingAfter(8f);
        doc.add(p);
    }
    private static void addSub(Document doc, String title) throws Exception {
        Paragraph p = new Paragraph(title, H2);
        p.setSpacingBefore(8f);
        p.setSpacingAfter(4f);
        doc.add(p);
    }

    // ---------------- INDIVIDUALES ----------------

    public static byte[] buildUsersPdf(String title, List<UserReportRow> rows){
        try{
            Document doc = new Document(PageSize.A4.rotate(), 24,24,24,24);
            ByteArrayOutputStream out = start(doc);
            addTitle(doc, title);

            PdfPTable t = new PdfPTable(new float[]{10,22,18,30,18,14,14});
            t.setWidthPercentage(100);
            t.addCell(th("ID"));
            t.addCell(th("NOMBRE"));
            t.addCell(th("USUARIO"));
            t.addCell(th("CORREO"));
            t.addCell(th("TELÉFONO"));
            t.addCell(th("ROL"));
            t.addCell(th("ESTADO"));

            for (UserReportRow r : rows){
                t.addCell(td(fmt((long) r.getUsuarioId())));
                t.addCell(td(r.getNombre()));
                t.addCell(td(r.getUsuario()));
                t.addCell(td(r.getCorreo()));
                t.addCell(td(r.getTelefono()));
                t.addCell(td(r.getRol()));
                t.addCell(td(r.getEstado()));
            }
            doc.add(t);
            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error users PDF", e); }
    }

    public static byte[] buildProductsPdf(String title, List<ProductReportRow> rows){
        try{
            Document doc = new Document(PageSize.A4.rotate(), 24,24,24,24);
            ByteArrayOutputStream out = start(doc);
            addTitle(doc, title);

            PdfPTable t = new PdfPTable(new float[]{10,38,14,12,14,14,10});
            t.setWidthPercentage(100);
            t.addCell(th("ID")); t.addCell(th("PRODUCTO")); t.addCell(th("P.UNIT."));
            t.addCell(th("STOCK")); t.addCell(th("COSTO")); t.addCell(th("FECHA")); t.addCell(th("OK"));

            for (ProductReportRow r : rows){
                t.addCell(td(fmt((long) r.getProductoId())));
                t.addCell(td(r.getNombre()));
                t.addCell(tdRight(fmt(r.getPrecioUnitario())));
                t.addCell(tdRight(fmt(r.getStock())));
                t.addCell(tdRight(fmt(r.getCosto())));
                t.addCell(td(fdd(r.getFecha())));
                t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
            }
            doc.add(t);
            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error products PDF", e); }
    }

    public static byte[] buildOrdersPdf(String title, List<OrderReportRow> rows){
        return simpleTablePdf(title, PageSize.A4.rotate(),
                new String[]{"ID","FECHA","USUARIO","SUBTOTAL","IVA","TOTAL"},
                new float[]{10,18,12,18,18,18},
                rows.size(),
                (t,i)->{
                    OrderReportRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getOrdenId())));
                    t.addCell(td(fdd(r.getFecha())));
                    t.addCell(td(fmt((long) r.getUsuarioId())));
                    t.addCell(tdRight(fmt(r.getSubtotal())));
                    t.addCell(tdRight(fmt(r.getIva())));
                    t.addCell(tdRight(fmt(r.getTotal())));
                });
    }

    public static byte[] buildLowStockPdf(String title, List<LowStockRow> rows){
        return simpleTablePdf(title, PageSize.A4.rotate(),
                new String[]{"ID","PRODUCTO","STOCK","P.UNIT.","FECHA","OK"},
                new float[]{10,38,12,16,16,8},
                rows.size(),
                (t,i)->{
                    LowStockRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getProductoId())));
                    t.addCell(td(r.getNombre()));
                    t.addCell(tdRight(fmt(r.getStock())));
                    t.addCell(tdRight(fmt(r.getPrecioUnitario())));
                    t.addCell(td(fdd(r.getFecha())));
                    t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
                });
    }

    public static byte[] buildSalesByProductPdf(String title, List<SalesByProductRow> rows){
        return simpleTablePdf(title, PageSize.A4.rotate(),
                new String[]{"ID","PRODUCTO","UNIDADES","SUBTOTAL","IVA","TOTAL"},
                new float[]{10,42,12,16,16,16},
                rows.size(),
                (t,i)->{
                    SalesByProductRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getProductoId())));
                    t.addCell(td(r.getProducto()));
                    t.addCell(tdRight(fmt(r.getUnidades())));
                    t.addCell(tdRight(fmt(r.getSubtotal())));
                    t.addCell(tdRight(fmt(r.getIva())));
                    t.addCell(tdRight(fmt(r.getTotal())));
                });
    }

    public static byte[] buildRolesPdf(String title, List<RoleReportRow> rows){
        return simpleTablePdf(title, PageSize.A4,
                new String[]{"ID","ROL","USUARIOS"},
                new float[]{14,56,14},
                rows.size(),
                (t,i)->{
                    RoleReportRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getRolId())));
                    t.addCell(td(r.getNombre()));
                    t.addCell(tdRight(fmt(r.getUsuarios())));
                });
    }

    public static byte[] buildCitiesPdf(String title, List<CityReportRow> rows){
        return simpleTablePdf(title, PageSize.A4,
                new String[]{"ID","CIUDAD","PROVINCIA ID","PROVINCIA"},
                new float[]{12,40,18,30},
                rows.size(),
                (t,i)->{
                    CityReportRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getCiudadId())));
                    t.addCell(td(r.getCiudad()));
                    t.addCell(td(fmt((long) r.getProvinciaId())));
                    t.addCell(td(r.getProvincia()));
                });
    }

    public static byte[] buildProvincesPdf(String title, List<ProvinceReportRow> rows){
        return simpleTablePdf(title, PageSize.A4,
                new String[]{"ID","PROVINCIA","OK"},
                new float[]{14,62,12},
                rows.size(),
                (t,i)->{
                    ProvinceReportRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getProvinciaId())));
                    t.addCell(td(r.getProvincia()));
                    t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
                });
    }

    public static byte[] buildPaymentMethodsPdf(String title, List<PaymentMethodRow> rows){
        return simpleTablePdf(title, PageSize.A4,
                new String[]{"ID","TIPO","USUARIO","OK"},
                new float[]{12,52,18,10},
                rows.size(),
                (t,i)->{
                    PaymentMethodRow r = rows.get(i);
                    t.addCell(td(fmt((long) r.getMetodopagoId())));
                    t.addCell(td(r.getTipo()));
                    t.addCell(td(fmt((long) r.getUsuarioId())));
                    t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
                });
    }

    // ---------------- RESUMEN / MULTI ----------------

    public byte[] buildResumenAdmin(AdminSummaryDTO dto, List<KardexRow> kardex,
                                    LocalDate desde, LocalDate hasta){
        try{
            Document doc = new Document(PageSize.A4.rotate(), 24,24,24,24);
            ByteArrayOutputStream out = start(doc);

            addTitle(doc, "Reporte consolidado");
            doc.add(new Paragraph("Período: " + fdd(desde) + " a " + fdd(hasta), TD));
            doc.add(Chunk.NEWLINE);

            PdfPTable s = new PdfPTable(new float[]{20,14,14,14,14,20});
            s.setWidthPercentage(100);
            s.addCell(th("Usuarios"));
            s.addCell(th("Productos"));
            s.addCell(th("Órdenes"));
            s.addCell(th("Detalles"));
            s.addCell(th("Mov. Inv."));
            s.addCell(th("Ventas"));
            s.addCell(tdRight(fmt(dto.getTotalUsuarios())));
            s.addCell(tdRight(fmt(dto.getTotalProductos())));
            s.addCell(tdRight(fmt(dto.getTotalOrdenes())));
            s.addCell(tdRight(fmt(dto.getTotalDetallesOrden())));
            s.addCell(tdRight(fmt(dto.getTotalMovimientosInventario())));
            s.addCell(tdRight(DF2.format(dto.getTotalVentas())));
            doc.add(s);

            if (kardex != null && !kardex.isEmpty()){
                addSub(doc, "Kardex simple");
                PdfPTable t = new PdfPTable(new float[]{18,30,12,10,14,14,14});
                t.setWidthPercentage(100);
                t.addCell(th("FECHA"));
                t.addCell(th("PRODUCTO"));
                t.addCell(th("MOV."));
                t.addCell(th("CANT."));
                t.addCell(th("P.UNIT."));
                t.addCell(th("SUBTOTAL"));
                t.addCell(th("TOTAL"));

                for (KardexRow r : kardex){
                    t.addCell(td(r.getFecha()==null ? "" : r.getFecha().toString().replace('T',' ')));
                    t.addCell(td(r.getProducto()));
                    t.addCell(td(r.getMovimiento()));
                    t.addCell(tdRight(fmt(r.getCantidad())));
                    t.addCell(tdRight(fmt(r.getPrecioUnitario())));
                    t.addCell(tdRight(fmt(r.getSubtotal())));
                    t.addCell(tdRight(fmt(r.getTotal())));
                }
                doc.add(t);
            }

            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error resumen PDF", e); }
    }

    public byte[] buildMultiWithKardexVal(AdminSummaryDTO dto,
                                          List<UserReportRow> users,
                                          List<ProductReportRow> products,
                                          List<OrderReportRow> orders,
                                          List<LowStockRow> lowStock,
                                          List<SalesByProductRow> salesByProduct,
                                          List<RoleReportRow> roles,
                                          List<CityReportRow> cities,
                                          List<ProvinceReportRow> provinces,
                                          List<PaymentMethodRow> paymentMethods,
                                          List<KardexValRow> kardexVal,
                                          LocalDate desde, LocalDate hasta){
        try{
            Document doc = new Document(PageSize.A4.rotate(), 24,24,24,24);
            ByteArrayOutputStream out = start(doc);

            addTitle(doc, "Reporte consolidado");
            doc.add(new Paragraph("Período: " + fdd(desde) + " a " + fdd(hasta), TD));
            doc.add(Chunk.NEWLINE);

            PdfPTable s = new PdfPTable(new float[]{20,14,14,14,14,20});
            s.setWidthPercentage(100);
            s.addCell(th("Usuarios"));
            s.addCell(th("Productos"));
            s.addCell(th("Órdenes"));
            s.addCell(th("Detalles"));
            s.addCell(th("Mov. Inv."));
            s.addCell(th("Ventas"));
            s.addCell(tdRight(fmt(dto.getTotalUsuarios())));
            s.addCell(tdRight(fmt(dto.getTotalProductos())));
            s.addCell(tdRight(fmt(dto.getTotalOrdenes())));
            s.addCell(tdRight(fmt(dto.getTotalDetallesOrden())));
            s.addCell(tdRight(fmt(dto.getTotalMovimientosInventario())));
            s.addCell(tdRight(DF2.format(dto.getTotalVentas())));
            doc.add(s);

            addSub(doc, "Usuarios");            doc.add(tableUsers(users));
            addSub(doc, "Productos");           doc.add(tableProducts(products));
            addSub(doc, "Órdenes");             doc.add(tableOrders(orders));
            addSub(doc, "Stock bajo");          doc.add(tableLowStock(lowStock));
            addSub(doc, "Ventas por producto"); doc.add(tableSalesByProduct(salesByProduct));
            addSub(doc, "Roles");               doc.add(tableRoles(roles));
            addSub(doc, "Ciudades");            doc.add(tableCities(cities));
            addSub(doc, "Provincias");          doc.add(tableProvinces(provinces));
            addSub(doc, "Métodos de pago");     doc.add(tablePaymentMethods(paymentMethods));

            if (kardexVal != null && !kardexVal.isEmpty()){
                addSub(doc, "Kardex Valorizado - PEPS");
                doc.add(tableKardexVal(kardexVal));
            }

            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error multi PDF", e); }
    }

    public byte[] buildKardexValorizadoPdf(String titulo, List<KardexValRow> rows){
        try{
            Document doc = new Document(PageSize.A4.rotate(), 24,24,24,24);
            ByteArrayOutputStream out = start(doc);
            addTitle(doc, titulo);
            doc.add(tableKardexVal(rows));
            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error kardex PDF", e); }
    }

    // ---------------- Tablas reusables ----------------

    private static PdfPTable tableUsers(List<UserReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{10,22,18,30,18,14,14});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("NOMBRE")); t.addCell(th("USUARIO"));
        t.addCell(th("CORREO")); t.addCell(th("TELÉFONO")); t.addCell(th("ROL")); t.addCell(th("ESTADO"));
        for (UserReportRow r : rows){
            t.addCell(td(fmt((long) r.getUsuarioId())));
            t.addCell(td(r.getNombre()));
            t.addCell(td(r.getUsuario()));
            t.addCell(td(r.getCorreo()));
            t.addCell(td(r.getTelefono()));
            t.addCell(td(r.getRol()));
            t.addCell(td(r.getEstado()));
        }
        return t;
    }

    private static PdfPTable tableProducts(List<ProductReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{10,38,14,12,14,14,10});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("PRODUCTO")); t.addCell(th("P.UNIT."));
        t.addCell(th("STOCK")); t.addCell(th("COSTO")); t.addCell(th("FECHA")); t.addCell(th("OK"));
        for (ProductReportRow r : rows){
            t.addCell(td(fmt((long) r.getProductoId())));
            t.addCell(td(r.getNombre()));
            t.addCell(tdRight(fmt(r.getPrecioUnitario())));
            t.addCell(tdRight(fmt(r.getStock())));
            t.addCell(tdRight(fmt(r.getCosto())));
            t.addCell(td(fdd(r.getFecha())));
            t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
        }
        return t;
    }

    private static PdfPTable tableOrders(List<OrderReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{10,18,12,18,18,18});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("FECHA")); t.addCell(th("USUARIO"));
        t.addCell(th("SUBTOTAL")); t.addCell(th("IVA")); t.addCell(th("TOTAL"));
        for (OrderReportRow r : rows){
            t.addCell(td(fmt((long) r.getOrdenId())));
            t.addCell(td(fdd(r.getFecha())));
            t.addCell(td(fmt((long) r.getUsuarioId())));
            t.addCell(tdRight(fmt(r.getSubtotal())));
            t.addCell(tdRight(fmt(r.getIva())));
            t.addCell(tdRight(fmt(r.getTotal())));
        }
        return t;
    }

    private static PdfPTable tableLowStock(List<LowStockRow> rows){
        PdfPTable t = new PdfPTable(new float[]{10,38,12,16,16,8});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("PRODUCTO")); t.addCell(th("STOCK"));
        t.addCell(th("P.UNIT.")); t.addCell(th("FECHA")); t.addCell(th("OK"));
        for (LowStockRow r : rows){
            t.addCell(td(fmt((long) r.getProductoId())));
            t.addCell(td(r.getNombre()));
            t.addCell(tdRight(fmt(r.getStock())));
            t.addCell(tdRight(fmt(r.getPrecioUnitario())));
            t.addCell(td(fdd(r.getFecha())));
            t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
        }
        return t;
    }

    private static PdfPTable tableSalesByProduct(List<SalesByProductRow> rows){
        PdfPTable t = new PdfPTable(new float[]{10,42,12,16,16,16});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("PRODUCTO")); t.addCell(th("UNIDADES"));
        t.addCell(th("SUBTOTAL")); t.addCell(th("IVA")); t.addCell(th("TOTAL"));
        for (SalesByProductRow r : rows){
            t.addCell(td(fmt((long) r.getProductoId())));
            t.addCell(td(r.getProducto()));
            t.addCell(tdRight(fmt(r.getUnidades())));
            t.addCell(tdRight(fmt(r.getSubtotal())));
            t.addCell(tdRight(fmt(r.getIva())));
            t.addCell(tdRight(fmt(r.getTotal())));
        }
        return t;
    }

    private static PdfPTable tableRoles(List<RoleReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{14,56,14});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("ROL")); t.addCell(th("USUARIOS"));
        for (RoleReportRow r : rows){
            t.addCell(td(fmt((long) r.getRolId())));
            t.addCell(td(r.getNombre()));
            t.addCell(tdRight(fmt(r.getUsuarios())));
        }
        return t;
    }

    private static PdfPTable tableCities(List<CityReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{12,40,18,30});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("CIUDAD")); t.addCell(th("PROVINCIA ID")); t.addCell(th("PROVINCIA"));
        for (CityReportRow r : rows){
            t.addCell(td(fmt((long) r.getCiudadId())));
            t.addCell(td(r.getCiudad()));
            t.addCell(td(fmt((long) r.getProvinciaId())));
            t.addCell(td(r.getProvincia()));
        }
        return t;
    }

    private static PdfPTable tableProvinces(List<ProvinceReportRow> rows){
        PdfPTable t = new PdfPTable(new float[]{14,62,12});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("PROVINCIA")); t.addCell(th("OK"));
        for (ProvinceReportRow r : rows){
            t.addCell(td(fmt((long) r.getProvinciaId())));
            t.addCell(td(r.getProvincia()));
            t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
        }
        return t;
    }

    private static PdfPTable tablePaymentMethods(List<PaymentMethodRow> rows){
        PdfPTable t = new PdfPTable(new float[]{12,52,18,10});
        t.setWidthPercentage(100);
        t.addCell(th("ID")); t.addCell(th("TIPO")); t.addCell(th("USUARIO")); t.addCell(th("OK"));
        for (PaymentMethodRow r : rows){
            t.addCell(td(fmt((long) r.getMetodopagoId())));
            t.addCell(td(r.getTipo()));
            t.addCell(td(fmt((long) r.getUsuarioId())));
            t.addCell(td(r.getHabilitado()!=null && r.getHabilitado() ? "Sí" : "No"));
        }
        return t;
    }

    private static PdfPTable tableKardexVal(List<KardexValRow> rows){
        PdfPTable t = new PdfPTable(new float[]{16,28,12,12,12,12,12,12});
        t.setWidthPercentage(100);
        t.addCell(th("FECHA"));
        t.addCell(th("DETALLE"));
        t.addCell(th("ENTRADAS CANT."));
        t.addCell(th("ENTRADAS C.T."));
        t.addCell(th("SALIDAS CANT."));
        t.addCell(th("SALIDAS C.T."));
        t.addCell(th("SALDOS CANT."));
        t.addCell(th("SALDOS C.T."));

        for (KardexValRow r : rows){
            t.addCell(td(fdt(r.getFecha())));
            t.addCell(td(r.getDetalle()));
            t.addCell(tdRight(fmt(blankIfZero(r.getEntCant()))));
            t.addCell(tdRight(fmt(blankIfZero(r.getEntTotal()))));
            t.addCell(tdRight(fmt(blankIfZero(r.getSalCant()))));
            t.addCell(tdRight(fmt(blankIfZero(r.getSalTotal()))));
            t.addCell(tdRight(fmt(blankIfZero(r.getSldCant()))));
            t.addCell(tdRight(fmt(blankIfZero(r.getSldTotal()))));
        }
        return t;
    }

    private static BigDecimal blankIfZero(BigDecimal v){
        return (v!=null && v.signum()==0) ? null : v;
    }

    private static byte[] simpleTablePdf(String title, Rectangle page,
                                         String[] headers, float[] widths,
                                         int rows,
                                         BiConsumer<PdfPTable,Integer> rowWriter){
        try{
            Document doc = new Document(page, 24,24,24,24);
            ByteArrayOutputStream out = start(doc);
            addTitle(doc, title);

            PdfPTable t = new PdfPTable(widths);
            t.setWidthPercentage(100);
            for (String h : headers) t.addCell(th(h));
            for (int i=0;i<rows;i++) rowWriter.accept(t,i);

            doc.add(t);
            doc.close();
            return out.toByteArray();
        }catch(Exception e){ throw new RuntimeException("Error simple table PDF", e); }
    }
}
