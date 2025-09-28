package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.ProductoRecienteMenuDto;
import com.example.tienda_tech.repository.ProductoRecientesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoRecientesService {

    private final ProductoRecientesRepo repo;

    public List<ProductoRecienteMenuDto> topRecientes(int limit) {
        int lim = (limit <= 0 || limit > 10) ? 5 : limit; // saneo simple
        return repo.listar(lim);
    }
}
