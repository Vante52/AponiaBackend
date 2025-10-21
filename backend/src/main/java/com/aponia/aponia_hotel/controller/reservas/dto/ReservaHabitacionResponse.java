package com.aponia.aponia_hotel.controller.reservas.dto;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaHabitacionResponse(
    String id,
    String codigo,
    String estado,
    LocalDateTime fechaCreacion,
    BigDecimal totalEstadia,
    HabitacionAsignadaDTO habitacionAsignada,
    String mensaje
) {
    /**
     * DTO anidado con información de la habitación asignada
     */
    public record HabitacionAsignadaDTO(
        String id,
        Integer numero,
        String tipoNombre
    ) {}

    /**
     * Convierte una entidad Reserva en un DTO de respuesta
     */
    public static ReservaHabitacionResponse fromReserva(Reserva reserva) {
        if (reserva.getEstancias() == null || reserva.getEstancias().isEmpty()) {
            throw new IllegalStateException("La reserva debe tener al menos una estancia");
        }
        
        Estancia estancia = reserva.getEstancias().get(0);
        
        if (estancia.getHabitacionAsignada() == null) {
            throw new IllegalStateException("La estancia debe tener una habitación asignada");
        }
        
        return new ReservaHabitacionResponse(
            reserva.getId(),
            reserva.getCodigo(),
            reserva.getEstado().name(),
            reserva.getFechaCreacion(),
            estancia.getTotalEstadia(),
            new HabitacionAsignadaDTO(
                estancia.getHabitacionAsignada().getId(),
                estancia.getHabitacionAsignada().getNumero(),
                estancia.getTipoHabitacion().getNombre()
            ),
            String.format("Reserva confirmada exitosamente. Se le asignó la habitación #%d (%s)",
                estancia.getHabitacionAsignada().getNumero(),
                estancia.getTipoHabitacion().getNombre()
            )
        );
    }
}