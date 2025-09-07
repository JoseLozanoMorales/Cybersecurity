package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.DireccionCreateRequest;
import com.example.tienda_tech.dto.DireccionDTO;
import com.example.tienda_tech.dto.DireccionUpdateRequest;
import com.example.tienda_tech.model.Ciudad;
import com.example.tienda_tech.model.Direccion;
// import com.example.tienda_tech.model.Provincia;
import com.example.tienda_tech.model.Usuario;
import com.example.tienda_tech.repository.CiudadRepository;
import com.example.tienda_tech.repository.DireccionRepository;
// import com.example.tienda_tech.repository.ProvinciaRepository;
import com.example.tienda_tech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CiudadRepository ciudadRepository;
    // private final ProvinciaRepository provinciaRepository;

    public DireccionService(
            DireccionRepository direccionRepository,
            UsuarioRepository usuarioRepository,
            CiudadRepository ciudadRepository
            ) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.ciudadRepository = ciudadRepository;
        // this.provinciaRepository = provinciaRepository;
    }

    @Transactional(readOnly = true)
    public List<DireccionDTO> listar(Integer usuarioId) {
        return direccionRepository.findByUsuario_UsuarioId(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DireccionDTO crear(Integer usuarioId, DireccionCreateRequest req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        Ciudad ciudad = (req.getCiudadId() != null)
                ? ciudadRepository.findById(req.getCiudadId())
                    .orElseThrow(() -> new IllegalArgumentException("Ciudad no existe"))
                : null;

        // Provincia provincia = (req.getProvinciaId() != null)
        //         ? provinciaRepository.findById(req.getProvinciaId())
        //             .orElseThrow(() -> new IllegalArgumentException("Provincia no existe"))
        //         : null;

        Direccion d = new Direccion();
        d.setUsuario(usuario);
        d.setCalle(req.getCalle());
        d.setReferencia(req.getReferencia());
        d.setCiudad(ciudad);
        // d.setProvincia(provincia);

        Direccion guardada = direccionRepository.save(d);
        return toDTO(guardada);
    }

    @Transactional
    public DireccionDTO actualizar(Integer usuarioId, Integer direccionId, DireccionUpdateRequest req) {
        Direccion d = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no existe"));

        if (d.getUsuario() == null || !d.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException("La dirección no pertenece al usuario");
        }

        if (req.getCalle() != null) d.setCalle(req.getCalle());
        if (req.getReferencia() != null) d.setReferencia(req.getReferencia());

        if (req.getCiudadId() != null) {
            Ciudad c = ciudadRepository.findById(req.getCiudadId())
                    .orElseThrow(() -> new IllegalArgumentException("Ciudad no existe"));
            d.setCiudad(c);
        }
        // if (req.getProvinciaId() != null) {
        //     Provincia p = provinciaRepository.findById(req.getProvinciaId())
        //             .orElseThrow(() -> new IllegalArgumentException("Provincia no existe"));
        //     d.setProvincia(p);
        // }

        Direccion guardada = direccionRepository.save(d);
        return toDTO(guardada);
    }

    @Transactional
    public void eliminar(Integer usuarioId, Integer direccionId) {
        Direccion d = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no existe"));

        if (d.getUsuario() == null || !d.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException("La dirección no pertenece al usuario");
        }
        direccionRepository.delete(d);
    }

    private DireccionDTO toDTO(Direccion d) {
        DireccionDTO dto = new DireccionDTO();
        dto.setDireccionId(d.getDireccionId());
        if (d.getUsuario() != null && d.getUsuario().getUsuarioId() != null) {
            dto.setUsuarioId(Long.valueOf(d.getUsuario().getUsuarioId()));
        }
        dto.setCalle(d.getCalle());
        dto.setReferencia(d.getReferencia());
        // dto.setProvinciaNombre(d.getProvincia() != null ? d.getProvincia().getNombre() : null);
        dto.setCiudadNombre(d.getCiudad() != null ? d.getCiudad().getNombre() : null);
        return dto;
    }
}
