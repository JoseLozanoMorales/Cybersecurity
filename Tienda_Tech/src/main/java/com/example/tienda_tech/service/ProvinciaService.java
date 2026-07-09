package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.ProvinciaDTO;
import com.example.tienda_tech.model.Provincia;
import com.example.tienda_tech.repository.ProvinciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvinciaService {

    private final ProvinciaRepository repo;

    @Transactional(readOnly = true)
    public List<ProvinciaDTO> listar() {
        return repo.findAll(Sort.by("nombre")).stream()
                .map(e -> {
                    ProvinciaDTO d = new ProvinciaDTO();
                    d.setProvinciaId(e.getProvinciaId());
                    d.setNombre(e.getNombre());
                    return d;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void crear(ProvinciaDTO dto) {
        String nombre = dto.getNombre() == null ? "" : dto.getNombre().trim();
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        repo.agregar(nombre);
    }

    @Transactional
    public void actualizar(Long id, ProvinciaDTO dto) {
        String nombre = dto.getNombre();
        // si viene vacío → pasa null; tu SP hace COALESCE
        String n = (nombre == null || nombre.trim().isEmpty()) ? null : nombre.trim();
        repo.editar(id, n);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.eliminar(id);
    }
}
