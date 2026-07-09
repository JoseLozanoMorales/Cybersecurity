package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.AlmacenamientoCreateRequest;
import com.example.tienda_tech.repository.AlmacenamientoSpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlmacenamientoService {

    private final AlmacenamientoSpRepository spRepo;

    public AlmacenamientoService(AlmacenamientoSpRepository spRepo) {
        this.spRepo = spRepo;
    }

    @Transactional
    public void crear(AlmacenamientoCreateRequest r) {
        spRepo.agregarAlmacenamiento(
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
}
