package com.aponia.aponia_hotel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

/**
 * Configuration record that centralises the management of the external API key.
 * The key is injected via environment variable (see {@code application.properties})
 * to avoid hard-coding secrets in code or UI layers.  Storing the key here keeps
 * the responsibility inside the backend and makes it easier to audit future usages.
 */
@ConfigurationProperties(prefix = "app.external.api")
public class ExternalApiProperties {

    /**
     * API key required by the external service that stores multimedia assets.
     * It must be provided at runtime; the repository intentionally leaves this value blank.
     */
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Helper method to avoid duplicating null/blank checks across the codebase.
     *
     * @return {@code true} when a non-empty API key has been provided.
     */
    public boolean isConfigured() {
        return key != null && !key.isBlank();
    }

    /**
     * Returns the API key wrapped in an {@link Optional} so that calling code can
     * expressively handle the absence of a configured value without risking NPEs.
     */
    public Optional<String> getOptionalKey() {
        return Optional.ofNullable(key).filter(s -> !s.isBlank());
    }
}
