package com.example.tienda_tech.controller;

import com.example.tienda_tech.service.CarritoService;
import com.example.tienda_tech.service.PaymentService;
import com.example.tienda_tech.service.PaypalClient;
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

    @PostMapping("/create-order")
    public Map<String,String> createOrder(
            @RequestHeader(value="X-User-Id", required=false) Integer uidHdr) throws Exception {
        Integer u = uidHdr;
        var r = carrito.resumen(u);
        var total = new BigDecimal(String.valueOf(r.getOrDefault("total","0")));
        var ref = "CART-" + u + "-" + System.currentTimeMillis();
        return Map.of(
                "orderId",     paypal.createOrder(total, ref),
                "clientToken", paypal.generateClientToken()
        );
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
        System.out.printf("[CAPTURE] uid=%s, order=%s, dir=%s, mp=%s%n", uidHdr, orderId, direccionId, metodopagoId);        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Falta orderId"));
        }
        var cap = paypal.captureOrder(orderId);
        var status = cap.path("status").asText("");
        if (!"COMPLETED".equalsIgnoreCase(status)) {
            return ResponseEntity.status(409).body(Map.of("ok", false, "status", status));
        }
        var out = payments.confirmarOrdenDesdeCarrito(uidHdr, direccionId, metodopagoId);

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
    }

    public record CaptureReq(String orderId, Integer direccionId, Integer metodopagoId) {}
}
