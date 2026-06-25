package com.example.tienda_tech.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── Cabeceras de seguridad HTTP ──────────────────────────────
                .headers(headers -> headers
                        // X-XSS-Protection: 1; mode=block
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        // X-Content-Type-Options: nosniff
                        .contentTypeOptions(cto -> {})
                        // X-Frame-Options: DENY — evita clickjacking
                        .frameOptions(frame -> frame.deny())
                        // Content-Security-Policy
                        // default-src 'self'        → solo recursos del mismo origen por defecto
                        // script-src  'self' + CDNs → permite JS propio y las CDNs que usa el proyecto
                        // style-src   'self' + CDNs → permite CSS propio y fuentes externas
                        // font-src    'self' + CDNs → permite fuentes de Google/Tabler
                        // img-src     'self' data:  → permite imágenes propias y data URIs (avatares)
                        // frame-ancestors 'none'    → refuerza el DENY de X-Frame-Options
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com 'unsafe-inline'; " +
                                        "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com 'unsafe-inline'; " +
                                        "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                                        "img-src 'self' data: blob:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none';"
                        ))
                )

                .authorizeHttpRequests(auth -> auth
                        // HTML y estáticos
                        .requestMatchers("/", "/index.html", "/*.html", "/favicon.ico").permitAll()
                        .requestMatchers("/assets/**", "/uploads/**",
                                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // endpoints públicos de auth (si los usas)
                        .requestMatchers("/api/login", "/auth/**").permitAll()

                        // 🔓 abre toda la API mientras pruebas (o al menos /api/**)
                        .requestMatchers("/api/**").permitAll()

                        // resto
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(true);
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}

