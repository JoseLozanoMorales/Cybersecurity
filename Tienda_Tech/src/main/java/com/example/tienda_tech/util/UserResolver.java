package com.example.tienda_tech.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserResolver {

    public static Integer resolveUserId(HttpServletRequest req) {
        // 1) Header
        String header = req.getHeader("X-User-Id");
        if (header != null && header.matches("\\d+")) {
            return Integer.parseInt(header);
        }

        // 2) Atributo (puede setearlo un filtro o interceptor)
        Object attr = req.getAttribute("usuarioId");
        if (attr instanceof Integer i) return i;
        if (attr instanceof String s && s.matches("\\d+")) return Integer.parseInt(s);

        // 3) Fallback dev (ajusta o borra en producción)
        return 3;
    }
}
