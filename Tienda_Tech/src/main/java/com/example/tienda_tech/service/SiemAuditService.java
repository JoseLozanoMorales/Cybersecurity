package com.example.tienda_tech.service;

import com.example.tienda_tech.model.SiemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class SiemAuditService {

    // Este logger escribe en siem-audit.log gracias al logback-spring.xml
    private static final Logger logger = LoggerFactory.getLogger(SiemAuditService.class);

    // Cola en memoria para el panel web en tiempo real (últimos 200 eventos)
    private final ConcurrentLinkedQueue<SiemEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private static final int MAX_EVENTS = 200;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra un evento SIEM:
     *  1. Lo guarda en la cola en memoria (para el panel web).
     *  2. Lo persiste en siem-audit.log con formato estructurado.
     *
     * @param tipo      Identificador del evento (ej. "LOGIN_EXITOSO", "BUSQUEDA_TEXTO")
     * @param usuario   Nombre o ID del usuario que generó el evento
     * @param modulo    Módulo del sistema (ej. "Carrito", "Búsqueda")
     * @param resultado Resultado de la acción (ej. "Exitoso", "Denegado")
     * @param detalle   Descripción legible del evento
     * @param nivel     Severidad: "INFO", "ADVERTENCIA" o "ALERTA"
     */
    public void registrarEvento(String tipo, String usuario, String modulo,
                                String resultado, String detalle, String nivel) {

        usuario = resolverNombreUsuario(usuario);

        tipo = safe(tipo); usuario = safe(usuario); modulo = safe(modulo);
        resultado = safe(resultado); detalle = safe(detalle); nivel = safe(nivel);
        String fecha = LocalDateTime.now().format(FORMATTER);
        String ip    = obtenerIPCliente();

        // ── 1. Cola en memoria ──────────────────────────────────────────
        SiemEvent evento = new SiemEvent(
                fecha, tipo,
                usuario  != null ? usuario  : "Desconocido",
                ip, modulo, resultado, nivel, detalle
        );
        eventQueue.add(evento);
        while (eventQueue.size() > MAX_EVENTS) {
            eventQueue.poll();
        }

        // ── 2. Persistencia en archivo de log ───────────────────────────
        // Formato de cada línea (pipe-separated para facilitar parsing con grep/awk/Splunk):
        // NIVEL | TIPO | USUARIO | IP | MODULO | RESULTADO | DETALLE
        String lineaLog = String.format(
                "%-11s | %-30s | %-20s | %-15s | %-15s | %-15s | %s",
                nivel, tipo,
                usuario  != null ? usuario  : "Desconocido",
                ip, modulo, resultado, detalle
        );

        if ("ALERTA".equalsIgnoreCase(nivel)) {
            logger.error(lineaLog);
        } else if ("ADVERTENCIA".equalsIgnoreCase(nivel)) {
            logger.warn(lineaLog);
        } else {
            logger.info(lineaLog);
        }
    }

    /** Retorna los eventos en memoria, del más reciente al más antiguo. */
    public List<SiemEvent> obtenerEventos() {
        List<SiemEvent> lista = new ArrayList<>(eventQueue);
        Collections.reverse(lista);
        return lista;
    }

    /** Limpia la cola en memoria (el archivo de log NO se borra). */
    public void limpiarEventos() {
        eventQueue.clear();
        registrarEvento(
                "SIEM_CLEAR", "Sistema", "Seguridad",
                "Exitoso", "Se limpió el historial en memoria. El archivo de log se conserva.", "INFO"
        );
    }

    // ── Utilidad: resolver el nombre de usuario legible para el log ─────
    // Varios controladores solo tienen a mano el usuario_id numérico (ej. String.valueOf(uid))
    // en vez del nombre de usuario. En esos casos, lo recuperamos de la cabecera X-Usuario
    // que auth-menu.js ya envía en cada fetch autenticado (ver auth-menu.js: getLoggedUsername()).
    private static final java.util.regex.Pattern SOLO_DIGITOS = java.util.regex.Pattern.compile("^\\d+$");

    private String resolverNombreUsuario(String usuarioPasado) {
        String limpio = usuarioPasado == null ? "" : usuarioPasado.trim();
        if (!limpio.isEmpty() && !SOLO_DIGITOS.matcher(limpio).matches()) {
            return limpio; // ya es un nombre de usuario/correo válido, no un ID crudo
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String header = request.getHeader("X-Usuario");
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
        }

        return "Anónimo";
    }

    // ── Utilidad: detectar IP real del cliente ──────────────────────────
    private String obtenerIPCliente() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return "127.0.0.1";
    }

    private String safe(String value) {
        if (value == null) return "Desconocido";
        return value.replace('\r', ' ').replace('\n', ' ').replace('|', '/').trim();
    }
}
