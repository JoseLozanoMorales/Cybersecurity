package com.example.tienda_tech.repository;

public interface AvatarRepository {
  void actualizarAvatar(int usuarioId, String avatarPath);
  void removerAvatar(int usuarioId);
}
