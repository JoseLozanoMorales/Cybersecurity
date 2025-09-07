package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.dto.CpuCreateRequest;
import com.example.tienda_tech.util.CapacidadNormalizer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProductoSpService {

    private final JdbcTemplate jdbc;

    // Constructor explícito (sin Lombok)
    public ProductoSpService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void crearAlmacenamiento(AlmacenamientoCreateRequest r) {
        // Ajusta la lista de ? si tu SP tiene más/menos parámetros
        jdbc.update(
            "CALL public.agregar_almacenamiento(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            r.getNombre(),
            r.getPreciounitario(),
            r.getEnlace(),
            r.getStock(),
            r.getMarca_id(),
            r.getGama_id(),
            r.getIva_id(),
            r.getCosto(),
            r.getCapacidad(),
            r.getTipo()
        );
    }

    @Transactional
    public void crearCpu(CpuCreateRequest r) {
        jdbc.update(
            "CALL public.agregar_cpu(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            r.getNombre(),
            r.getPreciounitario(),
            r.getEnlace(),
            r.getStock(),
            r.getMarca_id(),
            r.getGama_id(),
            r.getIva_id(),
            r.getCosto(),
            r.getSockets(),
            r.getGeneracion()
        );
    }


    @Transactional
    public void crearAlmacenamientoJsonV2(AlmacenamientoCreateRequest r) {
    var cap = CapacidadNormalizer.normalizeFromGb(
        r.getCapacidad() == null ? 0L : r.getCapacidad());

    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    var prod = mapper.createObjectNode();
    prod.put("nombre", r.getNombre());
    if (r.getEnlace() != null) prod.put("enlace", r.getEnlace());
    prod.put("marca_id", r.getMarca_id());
    prod.put("gama_id", r.getGama_id());
    prod.put("iva_id", r.getIva_id());
    prod.put("categoria_id", 1); // Almacenamiento
    prod.put("capacidad", cap.valor);
    prod.put("capacidad_unidad", cap.unidad); // "GB" o "TB"
    prod.put("tipo", r.getTipo());

    String payload = prod.toString();
    jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }

    @Transactional
    public void crearCpuJsonV2(com.example.tienda_tech.dto.CpuCreateRequest r) throws RuntimeException {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 2); // CPU
        if (r.getSockets() != null && !r.getSockets().isBlank()) prod.put("sockets", r.getSockets());
        if (r.getGeneracion() != null) prod.put("generacion", r.getGeneracion());

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }

    @org.springframework.transaction.annotation.Transactional
    public void crearCpuCoolerJsonV2(com.example.tienda_tech.dto.CpuCoolerCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 3); // CPU Cooler

        // específicos
        if (r.getTamanio() != null) prod.put("tamanio", r.getTamanio()); // BIGINT
        if (r.getSocket() != null && !r.getSocket().isBlank()) prod.put("socket", r.getSocket());

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }

    @org.springframework.transaction.annotation.Transactional
    public void crearCubiertaJsonV2(com.example.tienda_tech.dto.CubiertaCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 4); // Cubierta

        // específicos
        if (r.getTamanio_gpu() != null) prod.put("tamanio_gpu", r.getTamanio_gpu());
        if (r.getTamanio_refrigeracion() != null) prod.put("tamanio_refrigeracion", r.getTamanio_refrigeracion());

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }

    @org.springframework.transaction.annotation.Transactional
    public void crearFuenteJsonV2(com.example.tienda_tech.dto.FuenteCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // Comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 5); // Fuente de poder

        // Específico (OJO: clave con tilde para el SP)
        if (r.getConsumo_energia() != null) {
            prod.put("consumo_energía", r.getConsumo_energia());
        }

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }
    @org.springframework.transaction.annotation.Transactional
    public void crearGpuJsonV2(com.example.tienda_tech.dto.GpuCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // Comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 6); // GPU

        // Específicos
        if (r.getTamanio() != null) prod.put("tamanio", r.getTamanio());
        if (r.getConsumo_energia() != null) prod.put("consumo_energia", r.getConsumo_energia());

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }
    @org.springframework.transaction.annotation.Transactional
    public void crearRamJsonV2(com.example.tienda_tech.dto.RamCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // Comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id", r.getIva_id());
        prod.put("categoria_id", 7); // Memoria RAM

        // Específico
        if (r.getVelocidades() != null) prod.put("velocidades", r.getVelocidades());

        String payload = prod.toString();
        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", payload);
    }
    @org.springframework.transaction.annotation.Transactional
    public void crearMotherboardJsonV2(com.example.tienda_tech.dto.MotherboardCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // Comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace()!=null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id",  r.getIva_id());
        prod.put("categoria_id", 8); // Motherboard

        // Específicos
        if (r.getSocket()!=null && !r.getSocket().isBlank()) prod.put("socket", r.getSocket());
        if (r.getVelocidad_ram()!=null) prod.put("velocidad_ram", r.getVelocidad_ram());
        if (r.getChipset()!=null && !r.getChipset().isBlank()) prod.put("chipset", r.getChipset());

        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", prod.toString());
    }
    @org.springframework.transaction.annotation.Transactional
    public void crearPerifericoJsonV2(com.example.tienda_tech.dto.PerifericoCreateRequest r) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var prod = mapper.createObjectNode();

        // comunes
        prod.put("nombre", r.getNombre());
        if (r.getEnlace()!=null && !r.getEnlace().isBlank()) prod.put("enlace", r.getEnlace());
        prod.put("marca_id", r.getMarca_id());
        prod.put("gama_id", r.getGama_id());
        prod.put("iva_id",  r.getIva_id());
        prod.put("categoria_id", 9); // Periféricos

        // específico
        if (r.getTipo()!=null && !r.getTipo().isBlank()) prod.put("tipo", r.getTipo());

        jdbc.update("CALL public.sp_agregar_producto_v2_json(?::jsonb)", prod.toString());
    }

    @Transactional
    public void actualizarProductoJsonV2(Integer productoId, com.example.tienda_tech.dto.ProductoUpdateRequest r) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();

        obj.put("producto_id", productoId);

        // comunes
        if (r.getNombre() != null && !r.getNombre().isBlank()) obj.put("nombre", r.getNombre());
        if (r.getEnlace() != null && !r.getEnlace().isBlank()) obj.put("enlace", r.getEnlace());
        if (r.getMarca_id() != null) obj.put("marca_id", r.getMarca_id());
        if (r.getGama_id() != null)  obj.put("gama_id",  r.getGama_id());
        if (r.getIva_id() != null)   obj.put("iva_id",   r.getIva_id());
        if (r.getFecha() != null && !r.getFecha().isBlank()) obj.put("fecha", r.getFecha());
        if (r.getHabilitado() != null) obj.put("habilitado", r.getHabilitado());

        // Almacenamiento
        if (r.getCapacidad() != null) obj.put("capacidad", r.getCapacidad());
        if (r.getTipo() != null && !r.getTipo().isBlank()) obj.put("tipo", r.getTipo());
        if (r.getCapacidad_unidad() != null && !r.getCapacidad_unidad().isBlank()) {
            obj.put("capacidad_unidad", r.getCapacidad_unidad().toUpperCase()); // el SP castea al enum
        }

        // CPU
        if (r.getSockets() != null && !r.getSockets().isBlank()) obj.put("sockets", r.getSockets());
        if (r.getGeneracion() != null) obj.put("generacion", r.getGeneracion());

        // CPU Cooler
        if (r.getTamanio() != null) obj.put("tamanio", r.getTamanio());
        if (r.getSocket()  != null && !r.getSocket().isBlank()) obj.put("socket", r.getSocket());

        // Cubierta
        if (r.getTamanio_gpu() != null)           obj.put("tamanio_gpu", r.getTamanio_gpu());
        if (r.getTamanio_refrigeracion() != null) obj.put("tamanio_refrigeracion", r.getTamanio_refrigeracion());

        // Fuente de poder  (el SP espera 'consumo_energía' con tilde)
        if (r.getConsumo_energia() != null) {
            obj.put("consumo_energia",  r.getConsumo_energia());  // por si la categoría fuera GPU
            obj.put("consumo_energía", r.getConsumo_energia());   // por si es Fuente de poder
        }

        // RAM
        if (r.getVelocidades() != null) obj.put("velocidades", r.getVelocidades());

        // Motherboard
        if (r.getVelocidad_ram() != null) obj.put("velocidad_ram", r.getVelocidad_ram());
        if (r.getChipset() != null && !r.getChipset().isBlank()) obj.put("chipset", r.getChipset());

        String payload = obj.toString();
        jdbc.update("CALL public.sp_actualizar_producto_v2_json(?::jsonb)", payload);
    }









}
