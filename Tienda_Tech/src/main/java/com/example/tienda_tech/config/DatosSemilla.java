package com.example.tienda_tech.config;

import com.example.tienda_tech.model.Ciudad;
import com.example.tienda_tech.model.Provincia;
import com.example.tienda_tech.repository.CiudadRepository;
import com.example.tienda_tech.repository.ProvinciaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosSemilla {

    @Bean
    CommandLineRunner seedLosRios(ProvinciaRepository provinciaRepo, CiudadRepository ciudadRepo) {
        return args -> {
            // 1) Buscar provincia "Los Ríos" y si no existe, crearla
            Provincia prov = provinciaRepo.findAll().stream()
                    .filter(px -> px.getNombre() != null && px.getNombre().equalsIgnoreCase("Los Ríos"))
                    .findFirst()
                    .orElseGet(() -> {
                        Provincia np = new Provincia();
                        np.setNombre("Los Ríos");
                        return provinciaRepo.save(np);
                    });

            Short provId = prov.getProvinciaId(); // <- lo usamos para las ciudades

            // 2) Insertar ciudades (si no existen para esa provincia)
            String[] ciudades = {
                "Babahoyo","Baba","Buena Fe","Mocache","Montalvo","Palenque",
                "Puebloviejo","Quevedo","Quinsaloma","Urdaneta","Valencia","Ventanas","Vinces"
            };

            for (String nombre : ciudades) {
                boolean yaExiste = ciudadRepo.findAll().stream()
                        .anyMatch(c -> c.getNombre() != null
                                && c.getNombre().equalsIgnoreCase(nombre)
                                && c.getProvinciaId() != null
                                && c.getProvinciaId().equals(provId));
                if (!yaExiste) {
                    Ciudad c = new Ciudad();
                    c.setNombre(nombre);
                    c.setProvinciaId(provId);   // ← asigna la FK de la provincia "Los Ríos"
                    ciudadRepo.save(c);         // NO establezcas c.setCiudadId(...)

                }
            }
        };
    }
}
