package com.aponia.aponia_hotel.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Clave secreta utilizada para firmar los JWT. Debe tener suficiente entropía
     * (al menos 256 bits para HS256).
     */
    private String secret;

    /**
     * Tiempo de expiración en minutos del token.
     */
    private long expirationMinutes = 60;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public long getExpirationInMillis() {
        return expirationMinutes * 60_000L;
    }
}