package com.example.tienda_tech.dto;
import java.math.BigDecimal;
import java.util.List;

public class SugerenciaPcResponse {
    public Integer sugerenciaId;
    public String tipo;
    public BigDecimal presupuestoTotal;
    public BigDecimal totalPrecio;
    public Integer totalComponentes;
    public List<Item> items;

    public static class Item {
        public String categoria;
        public String producto;
        public Double precio;
        public Double score;
        public Boolean compatOk;
        public String motivo;
    }
}
