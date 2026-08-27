package com.example.tienda_tech.security;

import com.example.tienda_tech.service.SiemAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Detecta ráfagas de peticiones (muchas peticiones en pocos segundos) desde la
 * misma IP, las registra como alerta en el SIEM y, mientras dure la ráfaga,
 * BLOQUEA temporalmente esa IP devolviendo 429 (Too Many Requests) — ya no es
 * solo detección, también corta el tráfico, igual que ya hacía el límite de
 * reenvíos de OTP en OtpService.
 *
 * Importante: NO cuenta ni bloquea recursos que el propio navegador carga en
 * cadena sin que el usuario haga nada extra (CSS, JS, imágenes de galería).
 * Una sola vista de producto puede disparar varias de esas por sí sola, y
 * contarlas generaría falsos bloqueos con solo navegar normalmente. Por eso
 * solo se cuentan/bloquean peticiones "de acción" real contra la API.
 *
 * Nota para el proyecto: los mapas de historial/bloqueo por IP no se limpian
 * solos con el tiempo (las IPs que dejan de usarse se quedan como entradas
 * "frías" en memoria). Para una demo/entrega académica no es un problema,
 * pero en un entorno real conviene una tarea programada que las purgue.
 */
@Component
public class RequestRateMonitorFilter extends OncePerRequestFilter {

    // Ventana de tiempo que se analiza.
    private static final long VENTANA_MS = 5_000; // 5 segundos

    // Peticiones permitidas dentro de esa ventana antes de considerarlo sospechoso.
    private static final int UMBRAL_PETICIONES = 20;

    // Cuánto tiempo se bloquea una IP una vez que supera el umbral.
    private static final long DURACION_BLOQUEO_MS = 15_000; // 15 segundos

    // Rutas que el navegador carga "solo" (recursos estáticos e imágenes) y que
    // por lo tanto NO cuentan ni se bloquean.
    private static final Set<String> PREFIJOS_EXCLUIDOS = Set.of(
            "/css/", "/js/", "/img/", "/images/", "/assets/", "/webjars/",
            "/api/galeria/", "/api/galeria_v2/"
    );

    private final SiemAuditService siemAuditService;

    private final ConcurrentHashMap<String, Deque<Long>> historialPorIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> bloqueadoHastaPorIp = new ConcurrentHashMap<>();

    public RequestRateMonitorFilter(SiemAuditService siemAuditService) {
        this.siemAuditService = siemAuditService;
    }

    private boolean esRutaExcluida(String uri) {
        if ("/favicon.ico".equals(uri)) return true;
        for (String prefijo : PREFIJOS_EXCLUIDOS) {
            if (uri.startsWith(prefijo)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (esRutaExcluida(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long ahora = System.currentTimeMillis();

        // ── 1) ¿Esta IP ya está bloqueada por una ráfaga anterior? ──────────
        Long bloqueadoHasta = bloqueadoHastaPorIp.get(ip);
        if (bloqueadoHasta != null) {
            if (ahora < bloqueadoHasta) {
                rechazar(response, bloqueadoHasta - ahora);
                return; // no llega al resto de la aplicación
            }
            bloqueadoHastaPorIp.remove(ip); // el bloqueo ya expiró
        }

        // ── 2) Conteo de peticiones en la ventana de análisis ───────────────
        Deque<Long> historial = historialPorIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        historial.addLast(ahora);

        Long masAntiguo;
        while ((masAntiguo = historial.peekFirst()) != null && (ahora - masAntiguo) > VENTANA_MS) {
            historial.pollFirst();
        }

        int peticionesEnVentana = historial.size();

        // ── 3) Umbral superado: registrar alerta y bloquear ─────────────────
        if (peticionesEnVentana > UMBRAL_PETICIONES) {
            bloqueadoHastaPorIp.put(ip, ahora + DURACION_BLOQUEO_MS);
            historial.clear(); // reinicia el conteo para cuando se levante el bloqueo

            String usuarioHeader = request.getHeader("X-Usuario");
            String usuario = (usuarioHeader != null && !usuarioHeader.isBlank()) ? usuarioHeader : ip;

            siemAuditService.registrarEvento(
                    "POSIBLE_ATAQUE_FRECUENCIA",
                    usuario,
                    "Seguridad",
                    "Bloqueado",
                    "Se detectaron " + peticionesEnVentana + " peticiones en menos de "
                            + (VENTANA_MS / 1000) + " segundos desde la IP " + ip
                            + " (último recurso: " + request.getMethod() + " " + uri + "). "
                            + "Posible ataque de fuerza bruta o denegación de servicio (DoS). "
                            + "IP bloqueada durante " + (DURACION_BLOQUEO_MS / 1000) + " segundos.",
                    "ALERTA"
            );

            rechazar(response, DURACION_BLOQUEO_MS);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rechazar(HttpServletResponse response, long msRestantes) throws IOException {
        long segundosRestantes = Math.max(1, (msRestantes + 999) / 1000);
        response.setStatus(429); // HttpServletResponse no trae una constante para 429
        response.setHeader("Retry-After", String.valueOf(segundosRestantes));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"Demasiadas peticiones. Intenta de nuevo en " + segundosRestantes + " segundos.\"}"
        );
    }
}
