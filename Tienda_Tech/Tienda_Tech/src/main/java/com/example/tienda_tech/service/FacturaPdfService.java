package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacturaPdfService {

    private final FacturaService facturas;

    public byte[] render(Integer id) {
        Map<String,Object> data = facturas.obtenerFactura(id);
        Map<String,Object> e = (Map<String,Object>) data.get("encabezado");
        List<Map<String,Object>> items = (List<Map<String,Object>>) data.get("detalle");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ====== Fuentes + formato ======
            Font brand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Color brandColor = new Color(22, 119, 255);
            brand.setColor(brandColor);

            Font h6  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font txt = FontFactory.getFont(FontFactory.HELVETICA, 10);
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es","EC"));

            // ===== Encabezado: logo + marca pegados =====
            Image logo = drawComputerLogo(writer, brandColor);
            logo.scaleAbsolute(42, 30); // tamaño del icono

            Paragraph marca = new Paragraph();
            marca.setLeading(28f);
            marca.add(new Chunk(logo, 0, -7, true)); // pequeño ajuste de línea base
            marca.add(new Chunk("  TiendaTech", brand));     // dos espacios

            PdfPTable head = new PdfPTable(new float[]{6, 4});
            head.setWidthPercentage(100);

            PdfPCell marcaCell = new PdfPCell(marca);
            marcaCell.setBorder(Rectangle.NO_BORDER);
            marcaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell invCell = new PdfPCell();
            invCell.setBorder(Rectangle.NO_BORDER);
            invCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

// NUEVO: RUC arriba de "Factura"
            invCell.addElement(new Paragraph("RUC: 1291234565001", h6));

            invCell.addElement(new Paragraph(
                    "Factura " + (e.getOrDefault("numero", "#"+id)),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

            invCell.addElement(new Paragraph(
                    "Fecha: " + String.valueOf(e.getOrDefault("fechaemision","")), txt));
            head.addCell(marcaCell);
            head.addCell(invCell);
            doc.add(head);
            doc.add(Chunk.NEWLINE);

            // ====== Cliente / Entrega ======
            PdfPTable info = new PdfPTable(new float[]{1,1});
            info.setWidthPercentage(100);

            PdfPCell c1 = new PdfPCell(); c1.setBorder(Rectangle.NO_BORDER);
            c1.addElement(new Paragraph("Cliente", h6));
            c1.addElement(new Paragraph(String.valueOf(e.getOrDefault("nombre","")), txt));
            c1.addElement(new Paragraph(String.valueOf(e.getOrDefault("correo","")), txt));
            c1.addElement(new Paragraph(String.valueOf(e.getOrDefault("telefono","")), txt));
            info.addCell(c1);

            PdfPCell c2 = new PdfPCell(); c2.setBorder(Rectangle.NO_BORDER);
            c2.addElement(new Paragraph("Entrega", h6));
            c2.addElement(new Paragraph(String.valueOf(e.getOrDefault("direccionentrega","")), txt));
            info.addCell(c2);

            doc.add(info);
            doc.add(Chunk.NEWLINE);

            // ====== Detalle ======
            PdfPTable tbl = new PdfPTable(new float[]{3,1,1.2f,1.2f,0.8f,1.2f});
            tbl.setWidthPercentage(100);
            addHeader(tbl, "Producto","Cant.","Precio","Subtotal","IVA","Total");

            for (Map<String,Object> it : items) {
                tbl.addCell(cell(String.valueOf(it.getOrDefault("nombre_producto","")), txt, Element.ALIGN_LEFT));
                tbl.addCell(cell(String.valueOf(it.getOrDefault("cantidad",0)), txt, Element.ALIGN_RIGHT));
                tbl.addCell(cell(fmt.format(num(it.get("precio"))), txt, Element.ALIGN_RIGHT));
                tbl.addCell(cell(fmt.format(num(it.get("subtotal"))), txt, Element.ALIGN_RIGHT));
                tbl.addCell(cell(fmt.format(num(it.get("iva"))), txt, Element.ALIGN_RIGHT));
                tbl.addCell(cell(fmt.format(num(it.get("total"))), txt, Element.ALIGN_RIGHT));
            }
            doc.add(tbl);

            doc.add(Chunk.NEWLINE);

            // ====== Totales (compactados a la derecha) ======
            PdfPTable totals = new PdfPTable(new float[]{1,1});
            totals.setWidthPercentage(36);                        // un poco más cerca de los valores
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.addCell(noborder("Subtotal", h6, Element.ALIGN_LEFT));
            totals.addCell(noborder(fmt.format(num(e.get("subtotal"))), h6, Element.ALIGN_RIGHT));
            totals.addCell(noborder("Total", h6, Element.ALIGN_LEFT));
            totals.addCell(noborder(fmt.format(num(e.get("total"))), h6, Element.ALIGN_RIGHT));
            doc.add(totals);

        } catch (Exception ex) {
            throw new RuntimeException("No se pudo generar PDF", ex);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ====== Helpers ======
    private static Image drawComputerLogo(PdfWriter writer, Color color){
        float W = 60, H = 40;
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(W, H);

        tp.setLineWidth(2f);
        tp.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
        // marco de la pantalla
        tp.rectangle(2, 12, W-4, H-18);
        tp.stroke();

        // soporte y base
        tp.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
        tp.rectangle(W/2f - 3, 8, 6, 6);     // poste
        tp.fill();
        tp.rectangle(W/2f - 16, 4, 32, 3);   // base
        tp.fill();

        return Image.getInstance(tp);
    }

    private static double num(Object o){
        if (o == null) return 0d;
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(o));
    }
    private static void addHeader(PdfPTable t, String... labels){
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String s: labels){
            PdfPCell c = new PdfPCell(new Phrase(s, f));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(new Color(245,245,245));
            t.addCell(c);
        }
    }
    private static PdfPCell cell(String s, Font f, int align){
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setHorizontalAlignment(align);
        return c;
    }
    private static PdfPCell noborder(String s, Font f, int align){
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        return c;
    }
}
