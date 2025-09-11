package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.galeria.*;
import com.example.tienda_tech.model.Galeria;
import com.example.tienda_tech.model.GaleriaMedia;
import com.example.tienda_tech.repository.GaleriaMediaRepository;
import com.example.tienda_tech.repository.GaleriaRepository;
import com.example.tienda_tech.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GaleriaService {

  private final GaleriaRepository repo;            // JpaRepository<Galeria, Integer>
  private final GaleriaMediaRepository mediaRepo;  // JpaRepository<GaleriaMedia, Integer>
  private final ProductoRepository productoRepo;   // JpaRepository<Producto, Integer>

  public List<GaleriaItemDto> listar(long productoId){
    return repo.listar(productoId);
  }

  // <-- SIN @Override y con long para no romper el controller
  public List<Integer> subir(long productoId, List<GaleriaUploadItemDto> items){
    final int prodId = Math.toIntExact(productoId);

    log.info("[GAL] Subiendo {} item(s) para productoId={}", (items != null ? items.size() : 0), prodId);

    // Verificación de existencia (evita 23503 de FK)
    // Si añadiste el método nativo, puedes usar existsByIdNative(prodId)
    boolean exJpa = productoRepo.existsById(prodId);
    log.info("[GAL] productoId={} existe? jpa={}", prodId, exJpa);
    if (!exJpa) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe: " + prodId);
    }

    List<Integer> out = new ArrayList<>();
    Base64.Decoder dec = Base64.getDecoder();

    for (GaleriaUploadItemDto it : items){
      byte[] bytes = it.getContenidoBase64() != null ? dec.decode(it.getContenidoBase64()) : new byte[0];

      Galeria g = Galeria.builder()
          .productoId(prodId)   // Integer (int4)
          .descripcion(it.getDescripcion())
          .habilitado(Boolean.TRUE.equals(it.getHabilitado()))
          .paraGaleria(true)
          .paraMenu(Boolean.TRUE.equals(it.getParaMenu()))
          .mimeType(it.getMimeType())
          .pesoBytes((long) bytes.length)
          .build();

      g = repo.save(g); // INSERT en galeria_productos

      GaleriaMedia m = new GaleriaMedia(g);
      m.setContenido(bytes);
      mediaRepo.save(m);

      out.add(g.getGaleriaId()); // Integer
    }
    return out;
  }

  public void guardarFlags(List<GaleriaFlagDto> flags){
    for (GaleriaFlagDto f : flags){
      Integer id = toInt(f.getGaleriaId());
      Galeria g = repo.findById(id).orElseThrow();
      g.setParaGaleria(Boolean.TRUE.equals(f.getParaGaleria()));
      g.setParaMenu(Boolean.TRUE.equals(f.getParaMenu()));
      g.setHabilitado(Boolean.TRUE.equals(f.getHabilitado()));
      g.setDescripcion(f.getDescripcion());
    }
  }

  public void reordenar(long productoId, GaleriaReorderDto dto){
    if ("galeria".equalsIgnoreCase(dto.getVista())){
      for (GaleriaReorderDto.Item i : dto.getItems()){
        Integer id  = toInt(i.getGaleriaId());
        Integer pos = toInt(i.getPosicion());
        repo.updatePosGaleria(id, pos);
      }
    } else {
      for (GaleriaReorderDto.Item i : dto.getItems()){
        Integer id  = toInt(i.getGaleriaId());
        Integer pos = toInt(i.getPosicion());
        repo.updatePosMenu(id, pos);
      }
    }
  }

  public void marcarPortada(long productoId, int galeriaId){
    repo.marcarPortada(productoId, galeriaId);
  }

  @Transactional(Transactional.TxType.SUPPORTS)
  public Optional<Galeria> obtener(int galeriaId){
    return repo.findById(galeriaId);
  }

  public void eliminar(int galeriaId, boolean hard){
    if (hard){
      mediaRepo.deleteById(galeriaId);
      repo.deleteById(galeriaId);
    } else {
      repo.findById(galeriaId).ifPresent(g -> g.setHabilitado(false));
    }
  }

  /* ---------- helpers ---------- */
  private static Integer toInt(Number n){
    if (n == null) return null;
    if (n instanceof Integer i) return i;
    if (n instanceof Long l)    return Math.toIntExact(l);
    return n.intValue();
  }
}
