package com.example.tienda_tech.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.tienda_tech.dto.SugerenciaPcRequest;
import com.example.tienda_tech.dto.SugerenciaPcResponse;
import com.example.tienda_tech.model.VBuildHeader;
import com.example.tienda_tech.model.VBuildItem;
import com.example.tienda_tech.repository.SugerenciaSpRepo;
import com.example.tienda_tech.repository.VBuildHeaderRepo;
import com.example.tienda_tech.repository.VBuildItemsRepo;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class SugerenciaService {

    private final SugerenciaSpRepo sp;
    private final VBuildHeaderRepo headerRepo;
    private final VBuildItemsRepo itemsRepo;
    private final ObjectMapper mapper;

    public SugerenciaService(SugerenciaSpRepo sp, VBuildHeaderRepo headerRepo,
                             VBuildItemsRepo itemsRepo, ObjectMapper mapper) {
        this.sp = sp; this.headerRepo = headerRepo; this.itemsRepo = itemsRepo; this.mapper = mapper;
    }

    public SugerenciaPcResponse generarPcCompleta(String username, SugerenciaPcRequest req) {
        Integer sugId = sp.generarPcCompleta(
                mapper.valueToTree(req).toString(), // p_items::jsonb
                username,
                1,          // p_top_n (reservado)
                (short)1    // p_encuesta_id (reservado)
        );

        VBuildHeader h = headerRepo.findById(sugId)
                .orElseThrow(() -> new IllegalStateException("Sugerencia no encontrada: " + sugId));

        var items = itemsRepo.findAllBySugerenciaIdOrderByCategoriaAsc(sugId);

        var resp = new SugerenciaPcResponse();
        resp.sugerenciaId = h.getSugerenciaId();
        resp.tipo = h.getTipo();
        resp.presupuestoTotal = h.getPresupuestoTotal();
        resp.totalPrecio = h.getTotalPrecio();
        resp.totalComponentes = h.getTotalComponentes();
        resp.items = items.stream().map(i -> {
            var it = new SugerenciaPcResponse.Item();
            it.categoria = i.getCategoria();
            it.producto  = i.getProducto();
            it.precio    = i.getPrecio();
            it.score     = i.getScore();
            it.compatOk  = i.getCompatOk();
            it.motivo    = i.getMotivo();
            return it;
        }).collect(Collectors.toList());
        return resp;
    }
}
