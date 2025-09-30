package com.example.tienda_tech.report.service;

import com.example.tienda_tech.report.dto.*;
import com.example.tienda_tech.report.util.ReportPdfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final JdbcTemplate jdbcTemplate;
    private final ReportPdfUtil pdfUtil;

    // ----------------- Utiles -----------------
    private static BigDecimal Z(){ return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP); }

    // ============= RESUMEN =============
    @Override
    public AdminSummaryDTO loadAdminSummary(LocalDate desde, LocalDate hasta) {
        long totalUsuarios  = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.usuario", Long.class);
        long totalProductos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.producto", Long.class);
        long totalOrdenes   = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.orden o WHERE o.fecha BETWEEN ? AND ?",
                Long.class, java.sql.Date.valueOf(desde), java.sql.Date.valueOf(hasta));
        long totalDetalles  = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.detalle_orden d JOIN public.orden o ON o.orden_id = d.orden_id " +
                        "WHERE o.fecha BETWEEN ? AND ?",
                Long.class, java.sql.Date.valueOf(desde), java.sql.Date.valueOf(hasta));
        Double ventas       = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(o.total),0) FROM public.orden o WHERE o.fecha BETWEEN ? AND ?",
                Double.class, java.sql.Date.valueOf(desde), java.sql.Date.valueOf(hasta));
        long totalMovInv    = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.movimiento_inventario", Long.class);

        AdminSummaryDTO dto = new AdminSummaryDTO();
        dto.setTotalUsuarios(totalUsuarios);
        dto.setTotalProductos(totalProductos);
        dto.setTotalOrdenes(totalOrdenes);
        dto.setTotalDetallesOrden(totalDetalles);
        dto.setTotalMovimientosInventario(totalMovInv);
        dto.setTotalVentas(ventas != null ? ventas : 0d);
        return dto;
    }

    // ============= USUARIOS =============
    @Override
    public List<UserReportRow> loadUsers(LocalDate d1, LocalDate d2) {
        final String sql =
                "SELECT u.usuario_id, u.nombre, u.usuario, u.correo, u.telefono, " +
                        "       COALESCE(r.nombre, '(sin rol)') AS rol, " +
                        "       CASE WHEN COALESCE(u.habilitado, true) THEN 'Activo' ELSE 'Inactivo' END AS estado " +
                        "FROM public.usuario u LEFT JOIN public.rol r ON r.rol_id = u.rol_id " +
                        "ORDER BY u.usuario_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            UserReportRow r = new UserReportRow();
            r.setUsuarioId(rs.getInt("usuario_id"));
            r.setNombre(rs.getString("nombre"));
            r.setUsuario(rs.getString("usuario"));
            r.setCorreo(rs.getString("correo"));
            r.setTelefono(rs.getString("telefono"));
            r.setRol(rs.getString("rol"));
            r.setEstado(rs.getString("estado"));
            return r;
        });
    }

    // ============= PRODUCTOS =============
    @Override
    public List<ProductReportRow> loadProducts() {
        final String sql =
                "SELECT producto_id, nombre, preciounitario, stock, costo, fecha, habilitado " +
                        "FROM public.producto ORDER BY producto_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            ProductReportRow r = new ProductReportRow();
            r.setProductoId(rs.getInt("producto_id"));
            r.setNombre(rs.getString("nombre"));
            r.setPrecioUnitario(rs.getBigDecimal("preciounitario"));
            r.setStock((Integer) rs.getObject("stock"));
            r.setCosto(rs.getBigDecimal("costo"));
            r.setFecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null);
            r.setHabilitado((Boolean) rs.getObject("habilitado"));
            return r;
        });
    }

    // ============= ÓRDENES =============
    @Override
    public List<OrderReportRow> loadOrders(LocalDate d1, LocalDate d2) {
        final String sql =
                "SELECT o.orden_id, o.fecha, o.usuario_id, o.subtotal, " +
                        "       COALESCE(o.total - o.subtotal, 0) AS iva, o.total " +
                        "FROM public.orden o WHERE o.fecha BETWEEN ? AND ? ORDER BY o.fecha, o.orden_id";
        return jdbcTemplate.query(sql,
                ps -> {
                    ps.setDate(1, java.sql.Date.valueOf(d1));
                    ps.setDate(2, java.sql.Date.valueOf(d2));
                },
                (rs, n) -> {
                    OrderReportRow r = new OrderReportRow();
                    r.setOrdenId(rs.getInt("orden_id"));
                    r.setFecha(rs.getDate("fecha").toLocalDate());
                    r.setUsuarioId(rs.getInt("usuario_id"));
                    r.setSubtotal(rs.getBigDecimal("subtotal"));
                    r.setIva(rs.getBigDecimal("iva"));
                    r.setTotal(rs.getBigDecimal("total"));
                    return r;
                });
    }

    // ============= STOCK BAJO =============
    @Override
    public List<LowStockRow> loadLowStock(int limiteStock) {
        final String sql =
                "SELECT producto_id, nombre, stock, preciounitario, fecha, habilitado " +
                        "FROM public.producto WHERE stock <= ? ORDER BY stock ASC";
        return jdbcTemplate.query(sql, ps -> ps.setInt(1, limiteStock),
                (rs, n) -> {
                    LowStockRow r = new LowStockRow();
                    r.setProductoId(rs.getInt("producto_id"));
                    r.setNombre(rs.getString("nombre"));
                    r.setStock((Integer) rs.getObject("stock"));
                    r.setPrecioUnitario(rs.getBigDecimal("preciounitario"));
                    r.setFecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null);
                    r.setHabilitado((Boolean) rs.getObject("habilitado"));
                    return r;
                });
    }

    // ============= VENTAS POR PRODUCTO =============
    @Override
    public List<SalesByProductRow> loadSalesByProduct(LocalDate d1, LocalDate d2) {
        final String sql =
                "SELECT d.producto_id, COALESCE(p.nombre,'(sin nombre)') AS producto, " +
                        "       SUM(d.cantidad) AS unidades, SUM(d.subtotal) AS subtotal, " +
                        "       SUM(d.iva) AS iva, SUM(d.total) AS total " +
                        "FROM public.detalle_orden d " +
                        "JOIN public.orden o ON o.orden_id = d.orden_id " +
                        "LEFT JOIN public.producto p ON p.producto_id = d.producto_id " +
                        "WHERE o.fecha BETWEEN ? AND ? " +
                        "GROUP BY d.producto_id, p.nombre ORDER BY unidades DESC";
        return jdbcTemplate.query(sql,
                ps -> {
                    ps.setDate(1, java.sql.Date.valueOf(d1));
                    ps.setDate(2, java.sql.Date.valueOf(d2));
                },
                (rs, n) -> {
                    SalesByProductRow r = new SalesByProductRow();
                    r.setProductoId(rs.getInt("producto_id"));
                    r.setProducto(rs.getString("producto"));
                    r.setUnidades(rs.getInt("unidades"));
                    r.setSubtotal(rs.getBigDecimal("subtotal"));
                    r.setIva(rs.getBigDecimal("iva"));
                    r.setTotal(rs.getBigDecimal("total"));
                    return r;
                });
    }

    // ============= ROLES =============
    @Override
    public List<RoleReportRow> loadRoles() {
        final String sql =
                "SELECT r.rol_id AS id_rol, r.nombre, " +
                        "       (SELECT COUNT(*) FROM public.usuario u WHERE u.rol_id = r.rol_id) AS usuarios " +
                        "FROM public.rol r ORDER BY r.rol_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            RoleReportRow r = new RoleReportRow();
            r.setRolId(rs.getInt("id_rol"));
            r.setNombre(rs.getString("nombre"));
            r.setUsuarios(rs.getLong("usuarios"));
            return r;
        });
    }

    // ============= CIUDADES =============
    @Override
    public List<CityReportRow> loadCities() {
        final String sql =
                "SELECT c.ciudad_id, c.nombre AS ciudad, c.provincia_id, p.nombre AS provincia " +
                        "FROM public.ciudad c JOIN public.provincia p ON p.provincia_id = c.provincia_id " +
                        "ORDER BY c.ciudad_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            CityReportRow r = new CityReportRow();
            r.setCiudadId(rs.getInt("ciudad_id"));
            r.setCiudad(rs.getString("ciudad"));
            r.setProvinciaId(rs.getInt("provincia_id"));
            r.setProvincia(rs.getString("provincia"));
            return r;
        });
    }

    // ============= PROVINCIAS =============
    @Override
    public List<ProvinceReportRow> loadProvinces() {
        final String sql = "SELECT provincia_id, nombre, habilitado FROM public.provincia ORDER BY provincia_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            ProvinceReportRow r = new ProvinceReportRow();
            r.setProvinciaId(rs.getInt("provincia_id"));
            r.setProvincia(rs.getString("nombre"));
            r.setHabilitado((Boolean) rs.getObject("habilitado"));
            return r;
        });
    }

    // ============= MÉTODOS DE PAGO =============
    @Override
    public List<PaymentMethodRow> loadPaymentMethods() {
        final String sql =
                "SELECT m.metodopago_id AS id, t.nombre AS tipo, m.usuario_id, m.habilitado " +
                        "FROM public.metodopago m JOIN public.tipo_metodopago t ON t.tipo_id = m.tipo_id " +
                        "ORDER BY m.metodopago_id";
        return jdbcTemplate.query(sql, (rs, n) -> {
            PaymentMethodRow r = new PaymentMethodRow();
            r.setMetodopagoId(rs.getInt("id"));
            r.setTipo(rs.getString("tipo"));
            r.setUsuarioId(rs.getInt("usuario_id"));
            r.setHabilitado((Boolean) rs.getObject("habilitado"));
            return r;
        });
    }

    // ============= KARDEX VALORIZADO (PEPS en Java) =============
    private static record Mov(Integer prod, LocalDateTime fecha, String tipo,
                              BigDecimal cant, BigDecimal punit, String det) {}

    @Override
    public List<KardexValRow> loadKardexValorizado(LocalDate desde, LocalDate hasta, Integer productoId) {
        LocalDateTime d1 = desde.atStartOfDay();
        LocalDateTime d2 = hasta.atTime(LocalTime.MAX);

        String base =
                "SELECT m.producto_id, m.fecha, " +
                        "       CASE WHEN LOWER(sm.nombre) LIKE 'entrada%%' THEN 'ENTRADA' " +
                        "            WHEN LOWER(sm.nombre) LIKE 'salida%%'  THEN 'SALIDA'  " +
                        "            ELSE 'ENTRADA' END AS tipo, " +
                        "       CAST(m.cantidad AS NUMERIC(18,6)) AS cantidad, " +
                        "       CAST(m.costo_unitario AS NUMERIC(18,6)) AS punit, " +
                        "       COALESCE(m.referencia, m.observacion, '') AS detalle " +
                        "FROM public.movimiento_inventario m " +
                        "LEFT JOIN public.subtipo_movimiento sm ON sm.subtipo_id = m.subtipo_id " +
                        "WHERE m.fecha BETWEEN ? AND ? ";
        String sql = (productoId != null)
                ? base + " AND m.producto_id = ? ORDER BY m.producto_id, m.fecha"
                : base + " ORDER BY m.producto_id, m.fecha";

        Object[] params = (productoId != null)
                ? new Object[]{java.sql.Timestamp.valueOf(d1), java.sql.Timestamp.valueOf(d2), productoId}
                : new Object[]{java.sql.Timestamp.valueOf(d1), java.sql.Timestamp.valueOf(d2)};

        List<Mov> movs = jdbcTemplate.query(sql, params, (rs, i) -> new Mov(
                rs.getInt("producto_id"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getString("tipo"),
                rs.getBigDecimal("cantidad"),
                rs.getBigDecimal("punit"),
                rs.getString("detalle")
        ));

        // Agrupar por producto
        Map<Integer, List<Mov>> byProd = new LinkedHashMap<>();
        for (Mov m : movs) byProd.computeIfAbsent(m.prod(), k -> new ArrayList<>()).add(m);

        // Ejecutar PEPS
        List<KardexValRow> out = new ArrayList<>();
        for (var e : byProd.entrySet()) out.addAll(runPeps(e.getValue()));
        return out;
    }

    private List<KardexValRow> runPeps(List<Mov> movs){
        record Lote(BigDecimal cant, BigDecimal punit){}
        Deque<Lote> capas = new ArrayDeque<>();
        BigDecimal sCant  = Z();
        BigDecimal sTotal = Z();

        List<KardexValRow> rows = new ArrayList<>();
        for (Mov m : movs){
            KardexValRow r = new KardexValRow();
            r.setFecha(m.fecha());
            r.setDetalle(m.det());
            r.setEntCant(Z()); r.setEntTotal(Z());
            r.setSalCant(Z()); r.setSalTotal(Z());
            r.setSldCant(Z()); r.setSldTotal(Z());

            if ("ENTRADA".equalsIgnoreCase(m.tipo())){
                BigDecimal tot = m.cant().multiply(m.punit());
                capas.addLast(new Lote(m.cant(), m.punit()));
                sCant  = sCant.add(m.cant());
                sTotal = sTotal.add(tot);
                r.setEntCant(m.cant());
                r.setEntTotal(tot);
            } else {
                BigDecimal req  = m.cant();
                BigDecimal cost = Z();
                while (req.compareTo(BigDecimal.ZERO) > 0){
                    if (capas.isEmpty())
                        throw new IllegalStateException("Salida excede inventario disponible.");
                    Lote f = capas.peekFirst();
                    BigDecimal usar = f.cant().min(req);
                    BigDecimal c    = usar.multiply(f.punit());

                    BigDecimal rem = f.cant().subtract(usar);
                    capas.removeFirst();
                    if (rem.compareTo(BigDecimal.ZERO) > 0) capas.addFirst(new Lote(rem, f.punit()));

                    req   = req.subtract(usar);
                    cost  = cost.add(c);
                    sCant = sCant.subtract(usar);
                    sTotal= sTotal.subtract(c);
                }
                r.setSalCant(m.cant());
                r.setSalTotal(cost);
            }
            r.setSldCant(sCant.max(BigDecimal.ZERO));
            r.setSldTotal(sTotal.max(BigDecimal.ZERO));
            rows.add(r);
        }
        return rows;
    }

    // ============= PDFs =============
    @Override
    public byte[] buildAdminSummaryPdf(AdminSummaryDTO dto, List<KardexRow> kardex,
                                       LocalDate desde, LocalDate hasta) {
        return pdfUtil.buildResumenAdmin(dto, kardex, desde, hasta);
    }

    @Override
    public byte[] buildMultiReportPdf(AdminSummaryDTO dto,
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
                                      LocalDate desde,
                                      LocalDate hasta) {

        // Incluir Kardex Valorizado en el General
        List<KardexValRow> kardexVal = loadKardexValorizado(desde, hasta, null);

        return pdfUtil.buildMultiWithKardexVal(
                dto,
                users, products, orders, lowStock, salesByProduct,
                roles, cities, provinces, paymentMethods,
                kardexVal, desde, hasta
        );
    }

    @Override
    public byte[] buildKardexValorizadoPdf(String titulo, List<KardexValRow> rows) {
        return pdfUtil.buildKardexValorizadoPdf(titulo, rows);
    }
}
