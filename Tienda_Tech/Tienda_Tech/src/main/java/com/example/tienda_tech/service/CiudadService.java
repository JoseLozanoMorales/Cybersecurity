package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.CiudadDTO;
import com.example.tienda_tech.repository.CiudadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CiudadService {
    private final CiudadRepository repo;

    @Transactional(readOnly = true)
    public List<CiudadDTO> listar() {
        return repo.findAll(Sort.by("nombre")).stream().map(e -> {
            var d = new CiudadDTO();
            d.setCiudadId(e.getCiudadId());
            d.setNombre(e.getNombre());
            d.setProvinciaId(e.getProvinciaId());
            return d;
        }).toList();
    }

    @Transactional
    public void crear(CiudadDTO dto) {
        String nombre = dto.getNombre()==null ? "" : dto.getNombre().trim();
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.getProvinciaId()==null) throw new IllegalArgumentException("ProvinciaId es obligatorio");
        repo.agregar(nombre, dto.getProvinciaId());
    }

    @Transactional
    public void actualizar(Short id, CiudadDTO dto) {           // <-- Short
        if (dto.getProvinciaId()==null) {
            throw new IllegalArgumentException("ProvinciaId es obligatorio");
        }
        String n = (dto.getNombre()==null || dto.getNombre().trim().isEmpty()) ? null : dto.getNombre().trim();
        repo.editar(id, n, dto.getProvinciaId());                 // <-- siempre se envía provinciaId
    }

    @Transactional
    public void eliminar(Short id) { repo.eliminar(id); }
}
