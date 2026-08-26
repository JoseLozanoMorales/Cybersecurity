package com.example.tienda_tech.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserResolver {

    public static Integer resolveUserId(HttpServletRequest req) {
        // La identidad solo puede ser establecida por JwtAuthenticationFilter.
        Object attr = req.getAttribute("usuarioId");
        if (attr instanceof Integer i) return i;
        if (attr instanceof String s && s.matches("\\d+")) return Integer.parseInt(s);

        throw new IllegalStateException("Usuario no autenticado");
    }
}
