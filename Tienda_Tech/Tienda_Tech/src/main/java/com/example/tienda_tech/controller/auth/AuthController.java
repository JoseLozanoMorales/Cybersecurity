package com.example.tienda_tech.controller.auth;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import com.example.tienda_tech.service.auth.RefreshTokenService;

@RestController @RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService svc;

    @Value("${auth.cookie.domain}")   String cookieDomain;
    @Value("${auth.cookie.secure}")   boolean cookieSecure;
    @Value("${auth.cookie.samesite}") String cookieSameSite;

    private void writeRefreshCookie(HttpServletResponse res, String jwt, Instant absExp){
        ResponseCookie cookie = ResponseCookie.from("refresh", jwt)
                .httpOnly(true).secure(cookieSecure).domain(cookieDomain)
                .sameSite(cookieSameSite).path("/").maxAge(Duration.between(Instant.now(), absExp))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // Ejemplo: tu LoginController ya valida credenciales; aquí solo muestro el contrato
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body, HttpServletResponse res){
        // ... validar user & role desde tu UsuarioService ...
        Integer userId = /*...*/ 1;
        String username = /*...*/ "demo";
        String role = /*...*/ "TRABAJADOR";

        var out = svc.issueOnLogin(userId, username, role);
        writeRefreshCookie(res, out.refreshJwt(), out.absExp());

        return ResponseEntity.ok(Map.of(
                "access", out.access(),
                "user", Map.of("usuario_id", userId, "usuario", username, "rol", role)
        ));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refresh") String refresh, HttpServletResponse res){
        var r = svc.refresh(refresh);
        writeRefreshCookie(res, r.refreshJwt(), Instant.now().plusSeconds(3600*8)); // abs exp ya está dentro; maxAge lo recalcula
        return ResponseEntity.ok(Map.of("access", r.access(), "meta", r.meta()));
    }

    @PostMapping("/auth/keepalive")
    public ResponseEntity<?> keepalive(@CookieValue("refresh") String refresh, HttpServletResponse res){
        var r = svc.refresh(refresh); // misma lógica que refresh
        writeRefreshCookie(res, r.refreshJwt(), Instant.now().plusSeconds(3600*8));
        return ResponseEntity.ok(Map.of("access", r.access(), "meta", r.meta()));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@CookieValue(value="refresh", required=false) String refresh,
                                    HttpServletResponse res){
        if (refresh != null){
            var claims = new com.example.tienda_tech.security.JwtUtil("x").parse(refresh).getBody(); // Usa el bean real
            var family = UUID.fromString(String.valueOf(claims.get("family_id")));
            svc.logoutFamily(family);
        }
        // borrar cookie
        ResponseCookie gone = ResponseCookie.from("refresh", "")
                .path("/").maxAge(0).httpOnly(true).secure(cookieSecure).domain(cookieDomain).sameSite(cookieSameSite).build();
        res.addHeader(HttpHeaders.SET_COOKIE, gone.toString());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
