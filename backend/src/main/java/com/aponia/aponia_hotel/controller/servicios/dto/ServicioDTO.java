package com.aponia.aponia_hotel.controller.servicios.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServicioDTO(
        String id,
        String nombre,
        String descripcion,
        String lugar,
        BigDecimal precioPorPersona,
        Integer duracionMinutos,
        Integer capacidadMaxima,
        List<String> imagenesUrls
) {}
