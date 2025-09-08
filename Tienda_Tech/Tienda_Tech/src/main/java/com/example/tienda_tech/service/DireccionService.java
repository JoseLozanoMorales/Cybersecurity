package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.DireccionDTO;
import com.example.tienda_tech.model.Ciudad;
import com.example.tienda_tech.model.Direccion;
import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.repository.CiudadRepository;
import com.example.tienda_tech.repository.DireccionRepository;
import com.example.tienda_tech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionService {

    private final DireccionRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final CiudadRepository ciudadRepo;

    @Transactional(readOnly = true)
    public List<DireccionDTO> listar(Integer usuarioId){
        return repo.findByUsuario_UsuarioIdAndHabilitadoTrue(usuarioId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public DireccionDTO crear(Integer usuarioId, DireccionDTO dto){
        if (dto.getCiudadId()==null) throw new IllegalArgumentException("ciudadId es obligatorio");
        String calle = dto.getCalle()==null ? "" : dto.getCalle().trim();
        if (calle.isEmpty()) throw new IllegalArgumentException("calle es obligatoria");

        Usuario u = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        Ciudad c = ciudadRepo.findById(dto.getCiudadId())
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no existe"));

        Direccion d = new Direccion();
        d.setUsuario(u);
        d.setCiudad(c);
        d.setCalle(calle);
        d.setReferencia(dto.getReferencia()==null ? null : dto.getReferencia().trim());
        d.setHabilitado(true);

        return toDTO(repo.save(d));
    }

    @Transactional
    public DireccionDTO actualizar(Integer usuarioId, Short direccionId, DireccionDTO dto){ // <- Short
        Direccion d = repo.findById(direccionId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no existe"));

        if (d.getUsuario()==null || !d.getUsuario().getUsuarioId().equals(usuarioId))
            throw new IllegalStateException("La dirección no pertenece al usuario");

        if (dto.getCalle()!=null)      d.setCalle(dto.getCalle().trim());
        if (dto.getReferencia()!=null) d.setReferencia(dto.getReferencia().trim());

        if (dto.getCiudadId()!=null){
            Ciudad c = ciudadRepo.findById(dto.getCiudadId())
                    .orElseThrow(() -> new IllegalArgumentException("Ciudad no existe"));
            d.setCiudad(c);
        }
        return toDTO(repo.save(d));
    }

    @Transactional
    public void eliminar(Integer usuarioId, Short direccionId){ // <- Short
        Direccion d = repo.findById(direccionId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no existe"));
        if (d.getUsuario()==null || !d.getUsuario().getUsuarioId().equals(usuarioId))
            throw new IllegalStateException("La dirección no pertenece al usuario");

        d.setHabilitado(false); // soft-delete
        repo.save(d);
    }

    private DireccionDTO toDTO(Direccion d){
        DireccionDTO dto = new DireccionDTO();
        dto.setDireccionId(d.getDireccionId()); // Short
        dto.setUsuarioId(
                d.getUsuario() != null ? d.getUsuario().getUsuarioId() : null
        ); // Integer
        dto.setCiudadId(
                d.getCiudad() != null ? d.getCiudad().getCiudadId() : null
        ); // Short
        dto.setCiudadNombre(
                d.getCiudad() != null ? d.getCiudad().getNombre() : null
        );
        // si no manejas provincia en Ciudad, deja null
        dto.setProvinciaNombre(null);

        dto.setCalle(d.getCalle());
        dto.setReferencia(d.getReferencia());
        dto.setHabilitado(
                d.getHabilitado() != null ? d.getHabilitado() : Boolean.TRUE
        );
        return dto;
    }
}
