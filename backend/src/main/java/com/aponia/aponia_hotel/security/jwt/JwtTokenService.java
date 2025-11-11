package com.aponia.aponia_hotel.security.jwt;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    private byte[] signingKey() {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new IllegalStateException("No se ha configurado security.jwt.secret");
        }
        return properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.getExpirationInMillis());

        return Jwts.builder()
                .setSubject(usuario.getId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .signWith(Keys.hmacShaKeyFor(signingKey()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(signingKey()))
                .build()
                .parseClaimsJws(token);
    }

    public long getExpirationSeconds() {
        return properties.getExpirationInMillis() / 1000L;
    }
}