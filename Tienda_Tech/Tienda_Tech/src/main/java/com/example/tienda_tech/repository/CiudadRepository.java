// src/main/java/com/example/tienda_tech/repository/CiudadRepository.java
package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CiudadRepository extends JpaRepository<Ciudad, Short> {
    List<Ciudad> findByProvinciaId(Short provinciaId);
}
