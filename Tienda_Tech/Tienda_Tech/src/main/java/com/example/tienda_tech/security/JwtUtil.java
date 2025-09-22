package com.example.tienda_tech.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    private final Key key;
    public JwtUtil(String secret){ this.key = Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateAccess(Integer userId, String username, String role, int minutes){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(minutes*60L)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefresh(Integer userId, String role, UUID jti, UUID family, Instant absExp){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("family_id", family.toString())
                .setId(jti.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(absExp)) // no sirve para idle; sólo absoluto
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String jwt){ return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt); }
}
