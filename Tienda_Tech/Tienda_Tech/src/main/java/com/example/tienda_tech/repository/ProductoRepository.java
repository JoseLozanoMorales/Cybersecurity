package com.example.tienda_tech.repository;


import com.example.tienda_tech.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}