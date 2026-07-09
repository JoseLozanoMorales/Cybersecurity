// src/main/java/com/example/tienda_tech/security/JwtConfig.java
package com.example.tienda_tech.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    public JwtUtil jwtUtil(@Value("${auth.jwt.secret}") String secret) {
        log.info("JWT secret cargada ({} chars).", secret.length());
        return new JwtUtil(secret);
    }
}

