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

    private static final Logger logger = LoggerFactory.getLogger(SiemAuditService.class);
    private final ConcurrentLinkedQueue<SiemEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private static final int MAX_EVENTS = 200;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void registrarEvento(String tipo, String usuario, String modulo, String resultado, String detalle, String nivel) {
        String fecha = LocalDateTime.now().format(formatter);
        String ip = obtenerIPCliente();

        SiemEvent evento = new SiemEvent(fecha, tipo, usuario != null ? usuario : "Desconocido", ip, modulo, resultado, nivel, detalle);

        eventQueue.add(evento);
        while (eventQueue.size() > MAX_EVENTS) {
            eventQueue.poll();
        }

        String logMessage = String.format("SIEM_ALERT [%s] | Type: %s | User: %s | IP: %s | Mod: %s | Res: %s | Details: %s",
                nivel, tipo, usuario, ip, modulo, resultado, detalle);

        if ("ALERTA".equalsIgnoreCase(nivel)) {
            logger.error(logMessage);
        } else if ("ADVERTENCIA".equalsIgnoreCase(nivel)) {
            logger.warn(logMessage);
        } else {
            logger.info(logMessage);
        }
    }

    public List<SiemEvent> obtenerEventos() {
        List<SiemEvent> lista = new ArrayList<>(eventQueue);
        Collections.reverse(lista);
        return lista;
    }

    public void limpiarEventos() {
        eventQueue.clear();
        registrarEvento("SIEM_CLEAR", "Sistema", "Seguridad", "Exitoso", "Se limpio el historial de eventos en memoria.", "INFO");
    }

    private String obtenerIPCliente() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xHeader = request.getHeader("X-Forwarded-For");
            if (xHeader != null && !xHeader.isEmpty()) {
                return xHeader.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "127.0.0.1";
    }
}
