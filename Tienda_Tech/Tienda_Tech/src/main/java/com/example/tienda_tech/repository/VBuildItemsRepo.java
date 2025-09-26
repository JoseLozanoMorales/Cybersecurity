package com.example.tienda_tech.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.tienda_tech.model.VBuildItem;
import com.example.tienda_tech.model.VBuildItemId;

public interface VBuildItemsRepo extends JpaRepository<VBuildItem, VBuildItemId> {
    List<VBuildItem> findAllBySugerenciaIdOrderByCategoriaAsc(Integer sugerenciaId);
}
