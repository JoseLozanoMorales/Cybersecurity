package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.GaleriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GaleriaRepository extends JpaRepository<GaleriaProducto, Long> {

    // Devuelve proyección (o la entidad) según el Class pasado
    <T> Optional<T> findByGaleriaIdAndHabilitadoTrue(Long galeriaId, Class<T> type);
}
