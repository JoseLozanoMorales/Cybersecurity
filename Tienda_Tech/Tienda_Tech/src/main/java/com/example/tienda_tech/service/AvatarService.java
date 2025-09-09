package com.example.tienda_tech.service;

import com.example.tienda_tech.repository.AvatarRepository;
import org.springframework.stereotype.Service;

@Service
public class AvatarService {

  private final AvatarRepository repo;

  public AvatarService(AvatarRepository repo) {
    this.repo = repo;
  }

  public void actualizarAvatar(int usuarioId, String publicUrl) {
    repo.actualizarAvatar(usuarioId, publicUrl);
  }

  public void removerAvatar(int usuarioId) {
    repo.removerAvatar(usuarioId);
  }
}
