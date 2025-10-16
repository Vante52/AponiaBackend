package com.aponia.aponia_hotel.controller.reservas.dto;

import java.time.LocalDate;

public record ReservaHabitacionRequest(
        String tipoHabitacionId,
        LocalDate entrada,
        LocalDate salida,
        Integer numeroHuespedes,
        String notas
) {}
