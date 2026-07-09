package com.example.tienda_tech.service;


import com.example.tienda_tech.dto.ProductoListDTO;
import com.example.tienda_tech.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductoService {
private final ProductoRepository repo;


public Page<ProductoListDTO> listar(Pageable pageable){
return repo.findAll(pageable).map(ProductoListDTO::fromEntity);
}
}