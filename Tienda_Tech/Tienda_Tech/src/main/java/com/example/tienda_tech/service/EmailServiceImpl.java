package com.example.tienda_tech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;

  @Override
  public void enviarCredenciales(String to, String usuario, String password) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject("Tu cuenta en TiendaTech");
    msg.setText("""
        ¡Hola!

        Se ha creado tu cuenta en TiendaTech.
        Usuario: %s
        Contraseña: %s

        Por seguridad, cambia tu contraseña al iniciar sesión.

        Saludos,
        TiendaTech
        """.formatted(usuario, password));
    mailSender.send(msg);
  }
}
