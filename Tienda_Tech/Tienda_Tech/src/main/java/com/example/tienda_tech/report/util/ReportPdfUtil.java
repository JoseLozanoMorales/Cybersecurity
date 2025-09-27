package com.example.tienda_tech.report.util;

import com.example.tienda_tech.report.dto.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Utilidad de PDFs sin dependencias AWT (solo com.lowagie.*).
 * Incluye las 3 APIs clásicas:
 *  - buildResumenAdmin(...)
 *  - buildMulti(...)
 *  - buildKardexValorizado(...)
 * y constructores de PDFs individuales usados por el panel:
 *  - buildUsersPdf(...), buildProductsPdf(...), buildOrdersPdf(...), etc.
 */
@Component
public class ReportPdfUtil {

    // ======== fuentes ========
    private static final Font TITLE = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font H2    = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font TH    = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font TD    = new Font(Font.HELVETICA, 9, Font.NORMAL);

    // ---------------------------------------------------------------------------------
    // 1) Resumen Admin (encabezado + una tabla de kardex simple)
    // ---------------------------------------------------------------------------------
    public byte[] buildResumenAdmin(AdminSummaryDTO dto,
                                    List<KardexRow> kardex,
                                    LocalDate desde,
                                    LocalDate hasta) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();

            doc.add(new Paragraph("Reporte General Administrativo - TiendaTech", TITLE));
            doc.add(new Paragraph("Periodo: " + desde + " a " + hasta));
            doc.add(new Paragraph(" "));

            // bloque resumen
            summaryBlock(doc, "Usuarios registrados", String.valueOf(dto.getTotalUsuarios()));
            summaryBlock(doc, "Productos en catálogo", String.valueOf(dto.getTotalProductos()));
            summaryBlock(doc, "Órdenes",              String.valueOf(dto.getTotalOrdenes()));
            summaryBlock(doc, "Detalles de orden",    String.valueOf(dto.getTotalDetallesOrden()));
            summaryBlock(doc, "Movimientos inventario", String.valueOf(dto.getTotalMovimientosInventario()));
            summaryBlock(doc, "Ventas totales",       String.valueOf(dto.getTotalVentas()));

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Kardex (ventas como SALIDA)", H2));
            doc.add(beanTable(
                    new String[]{"Fecha","ProductoID","Producto","Mov","Cant","P.Unit","Subtotal","IVA","Total"},
                    kardex,
                    new String[]{"fecha","productoId","producto","movimiento","cantidad",
                            "precioUnitario","subtotal","iva","total"}
            ));

        } catch (DocumentException e) {
            // devolvemos lo generado
        } finally {
            doc.close();
        }
        return bos.toByteArray();
    }

    // ---------------------------------------------------------------------------------
    // 2) PDF General múltiple (todas las tablas)
    // ---------------------------------------------------------------------------------
    public byte[] buildMulti(AdminSummaryDTO dto,
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
                             LocalDate desde, LocalDate hasta) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();

            doc.add(new Paragraph("Reporte General Administrativo - TiendaTech", TITLE));
            doc.add(new Paragraph("Periodo: " + desde + " a " + hasta));
            doc.add(new Paragraph(" "));

            // resumen compacto
            PdfPTable resumen = new PdfPTable(6);
            resumen.setWidthPercentage(100);
            addCell(resumen, "Usuarios", TH);
            addCell(resumen, "Productos", TH);
            addCell(resumen, "Órdenes", TH);
            addCell(resumen, "Detalles", TH);
            addCell(resumen, "Mov. Inv.", TH);
            addCell(resumen, "Ventas", TH);
            addCell(resumen, String.valueOf(dto.getTotalUsuarios()));
            addCell(resumen, String.valueOf(dto.getTotalProductos()));
            addCell(resumen, String.valueOf(dto.getTotalOrdenes()));
            addCell(resumen, String.valueOf(dto.getTotalDetallesOrden()));
            addCell(resumen, String.valueOf(dto.getTotalMovimientosInventario()));
            addCell(resumen, String.valueOf(dto.getTotalVentas()));
            doc.add(resumen);

            doc.add(new Paragraph(" "));

            // secciones
            doc.add(new Paragraph("Usuarios", H2));
            doc.add(beanTable(
                    new String[]{"ID","Nombre","Usuario","Correo","Teléfono","Rol","Estado"},
                    users,
                    new String[]{"usuarioId","nombre","usuario","correo","telefono","rol","estado"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Productos", H2));
            doc.add(beanTable(
                    new String[]{"ID","Nombre","P.Unitario","Stock","Costo","Fecha","Habilitado"},
                    products,
                    new String[]{"productoId","nombre","precioUnitario","stock","costo","fecha","habilitado"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Órdenes", H2));
            doc.add(beanTable(
                    new String[]{"ID","Fecha","UsuarioID","Subtotal","IVA","Total"},
                    orders,
                    new String[]{"ordenId","fecha","usuarioId","subtotal","iva","total"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Stock bajo", H2));
            doc.add(beanTable(
                    new String[]{"ID","Nombre","Stock","P.Unitario","Fecha","Habilitado"},
                    lowStock,
                    new String[]{"productoId","nombre","stock","precioUnitario","fecha","habilitado"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Ventas por producto", H2));
            doc.add(beanTable(
                    new String[]{"ProdID","Producto","Unidades","Subtotal","IVA","Total"},
                    salesByProduct,
                    new String[]{"productoId","producto","unidades","subtotal","iva","total"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Roles", H2));
            doc.add(beanTable(
                    new String[]{"RolID","Nombre","Usuarios"},
                    roles,
                    new String[]{"rolId","nombre","usuarios"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Ciudades", H2));
            doc.add(beanTable(
                    new String[]{"CiudadID","Ciudad","ProvID","Provincia"},
                    cities,
                    new String[]{"ciudadId","ciudad","provinciaId","provincia"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Provincias", H2));
            doc.add(beanTable(
                    new String[]{"ProvID","Provincia","Habilitado"},
                    provinces,
                    new String[]{"provinciaId","provincia","habilitado"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Métodos de pago", H2));
            doc.add(beanTable(
                    new String[]{"ID","Tipo","UsuarioID","Habilitado"},
                    paymentMethods,
                    new String[]{"metodopagoId","tipo","usuarioId","habilitado"}
            ));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Kardex (ventas como SALIDA)", H2));
            doc.add(beanTable(
                    new String[]{"Fecha","ProductoID","Producto","Mov","Cant","P.Unit","Subtotal","IVA","Total"},
                    kardex,
                    new String[]{"fecha","productoId","producto","movimiento","cantidad",
                            "precioUnitario","subtotal","iva","total"}
            ));

        } catch (DocumentException e) {
            // ignoramos para devolver lo generado
        } finally {
            doc.close();
        }
        return bos.toByteArray();
    }

    // ---------------------------------------------------------------------------------
    // 3) Kardex Valorizado
    // ---------------------------------------------------------------------------------
    public byte[] buildKardexValorizado(String titulo, List<KardexValRow> rows) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();

            doc.add(new Paragraph(titulo, TITLE));
            doc.add(new Paragraph(" "));

            doc.add(beanTable(
                    new String[]{"Fecha","ProductoID","Producto","Mov","Cant","Precio","SaldoCant","SaldoTotal"},
                    rows,
                    new String[]{"fecha","productoId","producto","mov","cantidad","precio","saldoCant","saldoTotal"}
            ));

        } catch (DocumentException e) {
            // noop
        } finally {
            doc.close();
        }
        return bos.toByteArray();
    }

    // =================================================================================
    // Helpers de instancia (usados por los 3 constructores anteriores)
    // =================================================================================
    private void summaryBlock(Document doc, String label, String value) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(60);
        t.getDefaultCell().setPadding(4f);
        addCell(t, label, TH);
        addCell(t, value, TD);
        doc.add(t);
    }

    private void addCell(PdfPTable t, String text) {
        addCell(t, text, TD);
    }

    private void addCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, f));
        c.setPadding(4f);
        t.addCell(c);
    }

    /** Crea una tabla a partir de una lista de beans usando reflexión simple. */
    private PdfPTable beanTable(String[] headers, List<?> rows, String[] props) {
        PdfPTable t = new PdfPTable(headers.length);
        t.setWidthPercentage(100);
        for (String h : headers) addCell(t, h, TH);

        if (rows == null) return t;

        for (Object r : rows) {
            for (String p : props) {
                Object v = readProp(r, p);
                addCell(t, toStr(v));
            }
        }
        return t;
    }

    private Object readProp(Object bean, String name) {
        if (bean == null || name == null) return null;
        String base = name.substring(0,1).toUpperCase() + name.substring(1);
        String[] getters = new String[]{"get"+base, "is"+base};
        for (String m : getters) {
            try {
                Method mm = bean.getClass().getMethod(m);
                return mm.invoke(bean);
            } catch (Exception ignored) {}
        }
        try {
            Field f = bean.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(bean);
        } catch (Exception ignored) {}
        return null;
    }

    private String toStr(Object v) {
        if (v == null) return "";
        if (v instanceof LocalDate) return v.toString();
        if (v instanceof Boolean) return ((Boolean) v) ? "Sí" : "No";
        if (v instanceof BigDecimal) return ((BigDecimal) v).toPlainString();
        return Objects.toString(v, "");
    }

    // =================================================================================
    // NUEVOS CONSTRUCTORES DE PDFs INDIVIDUALES (estáticos)
    // Usados por /api/report/usuarios/pdf, /productos/pdf, etc.
    // =================================================================================

    public static byte[] buildUsersPdf(String titulo, List<UserReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ID","Nombre","Usuario","Correo","Teléfono","Rol","Estado"},
                rows,
                new String[]{"usuarioId","nombre","usuario","correo","telefono","rol","estado"});
    }

    public static byte[] buildProductsPdf(String titulo, List<ProductReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ID","Nombre","P.Unitario","Stock","Costo","Fecha","Habilitado"},
                rows,
                new String[]{"productoId","nombre","precioUnitario","stock","costo","fecha","habilitado"});
    }

    public static byte[] buildOrdersPdf(String titulo, List<OrderReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ID","Fecha","UsuarioID","Subtotal","IVA","Total"},
                rows,
                new String[]{"ordenId","fecha","usuarioId","subtotal","iva","total"});
    }

    public static byte[] buildLowStockPdf(String titulo, List<LowStockRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ID","Nombre","Stock","P.Unitario","Fecha","Habilitado"},
                rows,
                new String[]{"productoId","nombre","stock","precioUnitario","fecha","habilitado"});
    }

    public static byte[] buildSalesByProductPdf(String titulo, List<SalesByProductRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ProdID","Producto","Unidades","Subtotal","IVA","Total"},
                rows,
                new String[]{"productoId","producto","unidades","subtotal","iva","total"});
    }

    public static byte[] buildRolesPdf(String titulo, List<RoleReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"RolID","Nombre","Usuarios"},
                rows,
                new String[]{"rolId","nombre","usuarios"});
    }

    public static byte[] buildCitiesPdf(String titulo, List<CityReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"CiudadID","Ciudad","ProvID","Provincia"},
                rows,
                new String[]{"ciudadId","ciudad","provinciaId","provincia"});
    }

    public static byte[] buildProvincesPdf(String titulo, List<ProvinceReportRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ProvID","Provincia","Habilitado"},
                rows,
                new String[]{"provinciaId","provincia","habilitado"});
    }

    public static byte[] buildPaymentMethodsPdf(String titulo, List<PaymentMethodRow> rows){
        return simpleBeanPdf(titulo,
                new String[]{"ID","Tipo","UsuarioID","Habilitado"},
                rows,
                new String[]{"metodopagoId","tipo","usuarioId","habilitado"});
    }

    // ===== helpers estáticos para los PDF individuales =====
    private static byte[] simpleBeanPdf(String titulo, String[] headers, List<?> rows, String[] props){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try{
            PdfWriter.getInstance(doc, bos);
            doc.open();
            doc.add(new Paragraph(titulo, TITLE));
            doc.add(new Paragraph(" "));
            doc.add(beanTableS(headers, rows, props));
        }catch(DocumentException e){
            // devolver lo generado
        }finally{
            doc.close();
        }
        return bos.toByteArray();
    }

    private static PdfPTable beanTableS(String[] headers, List<?> rows, String[] props){
        PdfPTable t = new PdfPTable(headers.length);
        t.setWidthPercentage(100);
        for(String h: headers) addCellS(t, h, TH);

        if (rows != null){
            for(Object r: rows){
                for(String p: props){
                    Object v = readPropS(r, p);
                    addCellS(t, toStrS(v), TD);
                }
            }
        }
        return t;
    }

    private static void addCellS(PdfPTable t, String text, Font f){
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, f));
        c.setPadding(4f);
        t.addCell(c);
    }
    private static void addCellS(PdfPTable t, String text, Font f, int colspan){
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, f));
        c.setPadding(4f);
        c.setColspan(colspan);
        t.addCell(c);
    }
    private static void addCellS(PdfPTable t, String text, Font f, float padding){
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, f));
        c.setPadding(padding);
        t.addCell(c);
    }
    private static void addCellS(PdfPTable t, String text){
        addCellS(t, text, TD);
    }

    private static Object readPropS(Object bean, String name){
        if (bean == null || name == null) return null;
        String base = name.substring(0,1).toUpperCase() + name.substring(1);
        String[] getters = new String[]{"get"+base, "is"+base};
        for (String m : getters) {
            try {
                Method mm = bean.getClass().getMethod(m);
                return mm.invoke(bean);
            } catch (Exception ignored) {}
        }
        try {
            Field f = bean.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(bean);
        } catch (Exception ignored) {}
        return null;
    }

    private static String toStrS(Object v){
        if (v == null) return "";
        if (v instanceof LocalDate) return v.toString();
        if (v instanceof Boolean)   return ((Boolean) v) ? "Sí" : "No";
        if (v instanceof BigDecimal) return ((BigDecimal) v).toPlainString();
        return Objects.toString(v, "");
    }
}
