package com.example.tienda_tech.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    // recursos estáticos
                    .requestMatchers("/assets/**", "/uploads/**").permitAll()
                    // endpoints públicos
                    .requestMatchers("/api/usuarios/crear", "/api/otp").permitAll()
                    // lo demás requiere login
                    .anyRequest().permitAll()// << clave: TODO público por ahora
            );
    // No llames a formLogin() ni httpBasic() -> no hay /login por defecto
    return http.build();
  }
}

