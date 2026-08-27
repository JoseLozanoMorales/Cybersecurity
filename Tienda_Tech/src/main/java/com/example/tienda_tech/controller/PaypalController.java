package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.CarritoService;
import com.example.tienda_tech.service.PaymentService;
import com.example.tienda_tech.service.PaypalClient;
import com.example.tienda_tech.service.SiemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pagos/paypal")
public class PaypalController {

    private final PaypalClient paypal;
    private final PaymentService payments;
    private final CarritoService carrito;
    private final SiemAuditService siemAuditService;

    @PostMapping("/create-order")
    public Map<String,String> createOrder(
            @RequestHeader(value="X-User-Id", required=false) Integer uidHdr) throws Exception {
        Integer u = com.example.tienda_tech.security.AuthenticatedUser.id();
        var r = carrito.resumen(u);
        var total = new BigDecimal(String.valueOf(r.getOrDefault("total","0")));
        var ref = "CART-" + u + "-" + System.currentTimeMillis();

        try {
            String orderId = paypal.createOrder(total, ref);

            siemAuditService.registrarEvento(
                    "PAGO_INICIADO",
                    String.valueOf(u),
                    "Pagos",
                    "Exitoso",
                    "El usuario pasó a pagar su carrito (ref=" + ref + "). Total: $" + total + ". Orden PayPal=" + orderId + ".",
                    "INFO"
            );

            return Map.of(
                    "orderId",     orderId,
                    "clientToken", paypal.generateClientToken()
            );
        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "PAGO_ORDEN_ERROR",
                    String.valueOf(u),
                    "Pagos",
                    "Fallido",
                    "No se pudo crear la orden de pago (ref=" + ref + "): " + e.getMessage(),
                    "ALERTA"
            );
            throw e;
        }
    }

    // 1) SIN path param (el orderId viene en el body)
    @PostMapping("/capture")
    public ResponseEntity<?> captureBody(
            @RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
            @RequestBody CaptureReq body) throws Exception {
        System.out.println("HIT /api/pagos/paypal/capture (body)");
        return doCapture(uidHdr, body.orderId(), body.direccionId(), body.metodopagoId());
    }


    // 2) CON path param (por si algún día lo llamas así)
    @PostMapping("/capture/{orderId}")
    public ResponseEntity<?> capturePath(
            @RequestHeader(value="X-User-Id", required=false) Integer uidHdr,
            @PathVariable String orderId,
            @RequestBody(required=false) CaptureReq body) throws Exception {
        System.out.println("HIT /api/pagos/paypal/capture/{orderId}");
        return doCapture(uidHdr, orderId, body!=null?body.direccionId():null, body!=null?body.metodopagoId():null);
    }

    private ResponseEntity<?> doCapture(Integer uidHdr, String orderId, Integer direccionId, Integer metodopagoId) throws Exception {
        uidHdr = com.example.tienda_tech.security.AuthenticatedUser.id();
        System.out.printf("[CAPTURE] uid=%s, order=%s, dir=%s, mp=%s%n", uidHdr, orderId, direccionId, metodopagoId);        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Falta orderId"));
        }

        try {
            var cap = paypal.captureOrder(orderId);
            var status = cap.path("status").asText("");
            if (!"COMPLETED".equalsIgnoreCase(status)) {
                siemAuditService.registrarEvento(
                        "PAGO_RECHAZADO",
                        String.valueOf(uidHdr),
                        "Pagos",
                        "Denegado",
                        "PayPal no completó la orden " + orderId + " (status=" + status + ").",
                        "ALERTA"
                );
                return ResponseEntity.status(409).body(Map.of("ok", false, "status", status));
            }
            var out = payments.confirmarOrdenDesdeCarrito(uidHdr, direccionId, metodopagoId);

            siemAuditService.registrarEvento(
                    "PAGO_EXITOSO",
                    String.valueOf(uidHdr),
                    "Pagos",
                    "Exitoso",
                    "Pago confirmado (orden PayPal=" + orderId + "). Factura #" + out.getNumero()
                            + " (ID=" + out.getFacturaId() + "), total: $" + out.getTotal() + ".",
                    "INFO"
            );

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "status", status,
                    "ordenId",   out.getOrdenId(),
                    "subtotal",  out.getSubtotal(),
                    "impuestos", out.getImpuestos(),
                    "total",     out.getTotal(),
                    "facturaId", out.getFacturaId(),
                    "numero",    out.getNumero()
            ));
        } catch (Exception e) {
            siemAuditService.registrarEvento(
                    "PAGO_ERROR",
                    String.valueOf(uidHdr),
                    "Pagos",
                    "Fallido",
                    "Error al capturar/confirmar el pago (orden=" + orderId + "): " + e.getMessage(),
                    "ALERTA"
            );
            throw e;
        }
    }

    public record CaptureReq(String orderId, Integer direccionId, Integer metodopagoId) {}
}
