package com.example.tienda_tech.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestRateMonitorFilter requestRateMonitorFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           RequestRateMonitorFilter requestRateMonitorFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.requestRateMonitorFilter = requestRateMonitorFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // La API es stateless y autentica exclusivamente con Authorization: Bearer.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

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

                        // ── Cabeceras contra clonado/suplantación del sitio (phishing) ──
                        // Ningún encabezado puede impedir que alguien copie el HTML/CSS y lo
                        // publique en otro dominio (eso requiere medidas fuera del código: DNS,
                        // certificados, SPF/DKIM/DMARC en el correo, reportes a Google Safe
                        // Browsing, etc.). Lo que SÍ se puede controlar desde aquí es que ese
                        // clon no pueda funcionar igual que el sitio real ni aprovecharse de él:
                        //
                        // Referrer-Policy: no manda la URL completa (con posibles tokens) como
                        // referer cuando alguien sale del sitio hacia otro dominio.
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Referrer-Policy", "strict-origin-when-cross-origin"))
                        // Cross-Origin-Opener-Policy: same-origin — si un sitio clon abre el
                        // real en una ventana/pestaña emergente, no puede quedarse con una
                        // referencia (window.opener) para leerlo o manipularlo.
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Cross-Origin-Opener-Policy", "same-origin"))
                        // Cross-Origin-Resource-Policy: same-origin — impide que un dominio
                        // distinto (el clon) cargue directamente imágenes/CSS/JS del sitio real
                        // para hacerse pasar por él (ej. <img src="tu-dominio-real/logo.png">).
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Cross-Origin-Resource-Policy", "same-origin"))

                        // Content-Security-Policy
                        // default-src 'self'        → solo recursos del mismo origen por defecto
                        // script-src  'self' + CDNs → permite JS propio y las CDNs que usa el proyecto
                        // style-src   'self' + CDNs → permite CSS propio y fuentes externas
                        // font-src    'self' + CDNs → permite fuentes de Google/Tabler
                        // img-src     'self' data:  → permite imágenes propias y data URIs (avatares)
                        // frame-ancestors 'none'    → refuerza el DENY de X-Frame-Options
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                                        "script-src-attr 'none'; " +
                                        "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                                        "style-src-attr 'none'; " +
                                        "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                                        "img-src 'self' data: blob:; " +
                                        "connect-src 'self'; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "form-action 'self'; " +
                                        "frame-src 'none'; " +
                                        "frame-ancestors 'none'; " +
                                        "upgrade-insecure-requests;"
                        ))
                )

                .authorizeHttpRequests(auth -> auth
                        // HTML y estáticos
                        .requestMatchers("/", "/index.html", "/*.html", "/favicon.ico").permitAll()
                        .requestMatchers("/assets/**", "/uploads/**",
                                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/login", "/api/login/mfa", "/api/otp/**",
                                "/api/usuarios/crear", "/api/usuarios/recuperar-password",
                                "/api/seguridad/cambiar-password-token").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/productos/**", "/api/categorias/**", "/api/galeria/**",
                                "/api/galeria_v2/img/**", "/api/busqueda/**", "/api/provincias/**",
                                "/api/ciudades/**", "/api/tipo_metodopago", "/api/productos/recientes-menu").permitAll()
                        .requestMatchers("/api/siem/**", "/api/auditoria/**", "/api/auditorias/**",
                                "/api/report/**", "/api/usuarios/crear-usuarioAdmin",
                                "/api/usuarios/admin/**", "/api/usuarios/buscar/**",
                                "/api/encuesta/**").hasRole("ADMIN")
                        .requestMatchers("/api/sp/**", "/api/movimientos/**", "/api/subtipos-movimiento/**")
                                .hasAnyRole("ADMIN", "TRABAJADOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/sugerencias/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/sugerencias/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/sugerencias/*:toggle").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sugerencias/*").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestRateMonitorFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(false);
        c.setAllowedOrigins(List.of("http://localhost:8080", "http://127.0.0.1:8080"));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        // Lista blanca de cabeceras: las únicas que el frontend realmente envía.
        // Antes era "*" (cualquier cabecera); restringirla evita que un origen
        // cruzado no autorizado pueda enviar cabeceras arbitrarias hacia la API.
        c.setAllowedHeaders(List.of(
                "Authorization", "Content-Type",
                "X-Usuario", "X-User-Id", "X-Username", "X-XSRF-TOKEN"
        ));
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}

