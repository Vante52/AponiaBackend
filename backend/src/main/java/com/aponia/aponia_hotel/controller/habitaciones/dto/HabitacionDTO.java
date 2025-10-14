package com.aponia.aponia_hotel.controller.habitaciones.dto;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;

public record HabitacionDTO(
    String id,
    Integer numero,
    Boolean activa,
    String tipoId,
    String tipoNombre
) {
    public static HabitacionDTO fromEntity(Habitacion h) {
        HabitacionTipo tipo = h.getTipo();
        return new HabitacionDTO(
            h.getId(),
            h.getNumero(),
            h.getActiva(),
            tipo != null ? tipo.getId() : null,
            tipo != null ? tipo.getNombre() : null
        );
    }
}
