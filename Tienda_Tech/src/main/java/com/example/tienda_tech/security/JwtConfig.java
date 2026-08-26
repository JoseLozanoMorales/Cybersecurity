// src/main/java/com/example/tienda_tech/security/JwtConfig.java
package com.example.tienda_tech.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Bean
    public JwtUtil jwtUtil(@Value("${auth.jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }
}

