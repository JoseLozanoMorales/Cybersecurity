package com.example.tienda_tech.util;

public class MaskUtils {
    public static String maskLast4(String pan) {
        if (pan == null) return "****";
        pan = pan.trim();
        if (pan.length() < 4) return "****";
        return "**** " + pan.substring(pan.length() - 4);
    }
}
