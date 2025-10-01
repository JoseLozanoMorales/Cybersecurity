package com.example.tienda_tech.util;

public final class CapacidadNormalizer {

    private CapacidadNormalizer() { }

    public static class Capacidad {
        public long valor;
        public String unidad; // "GB" | "TB"
        public Capacidad(long valor, String unidad) {
            this.valor = valor;
            this.unidad = unidad;
        }
    }

    public static Capacidad normalizeFromGb(long valorGb) {
        if (valorGb >= 1000 && valorGb % 1000 == 0) {
            return new Capacidad(valorGb / 1000, "TB");
        }
        return new Capacidad(valorGb, "GB");
    }
}