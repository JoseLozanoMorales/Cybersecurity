package com.example.tienda_tech.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import com.example.tienda_tech.model.SugerenciaEntity;

public interface SugerenciaSpRepo extends JpaRepository<SugerenciaEntity, Integer> {

    // coincide con tu PROC Opción 1 (sin DEFAULTs) y con el nombre del OUT exacto
    @Procedure(
            procedureName = "sp_sugerencia_pc_completa_v2_json",
            outputParameterName = "o_sugerencia_id"
    )
    Integer generarPcCompleta(
            @Param("p_items") String pItemsJson,
            @Param("p_usuario") String usuario,
            @Param("p_top_n") Integer topN,
            @Param("p_encuesta_id") Short encuestaId
    );
}
