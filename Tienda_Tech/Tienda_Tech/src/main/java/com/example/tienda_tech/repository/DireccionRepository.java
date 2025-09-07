package com.example.tienda_tech.repository;

import com.example.tienda_tech.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
    // Lista todas las direcciones de un usuario (por su PK short)
    List<Direccion> findByUsuario_UsuarioId(Integer usuarioId);
}
