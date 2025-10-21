package com.aponia.aponia_hotel.controller.reservas.dto;

import java.time.LocalDate;

public record ReservaHabitacionRequest(
    String tipoHabitacionId,
    LocalDate entrada,
    LocalDate salida,
    Integer numeroHuespedes,
    String notas
) {
    // Validaciones básicas en el constructor
    public ReservaHabitacionRequest {
        if (tipoHabitacionId == null || tipoHabitacionId.isBlank()) {
            throw new IllegalArgumentException("El tipo de habitación es requerido");
        }
        if (entrada == null) {
            throw new IllegalArgumentException("La fecha de entrada es requerida");
        }
        if (salida == null) {
            throw new IllegalArgumentException("La fecha de salida es requerida");
        }
        if (numeroHuespedes == null || numeroHuespedes <= 0) {
            throw new IllegalArgumentException("El número de huéspedes debe ser positivo");
        }
    }
}