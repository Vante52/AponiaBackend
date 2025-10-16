package com.aponia.aponia_hotel.controller.reservas.dto;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaHabitacionResponse(
        String reservaId,
        String codigo,
        LocalDateTime fechaCreacion,
        Reserva.EstadoReserva estado,
        LocalDate entrada,
        LocalDate salida,
        Integer numeroHuespedes,
        String tipoHabitacionId,
        String tipoHabitacionNombre,
        BigDecimal precioPorNoche,
        BigDecimal totalEstadia,
        BigDecimal totalReserva
) {
    public static ReservaHabitacionResponse fromReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        List<Estancia> estancias = reserva.getEstancias();
        if (estancias == null || estancias.isEmpty()) {
            throw new IllegalArgumentException("La reserva no contiene estancias asociadas");
        }
        Estancia estancia = estancias.get(0);
        BigDecimal totalReserva = reserva.getResumenPago() != null
                ? reserva.getResumenPago().getTotalReserva()
                : null;

        return new ReservaHabitacionResponse(
                reserva.getId(),
                reserva.getCodigo(),
                reserva.getFechaCreacion(),
                reserva.getEstado(),
                estancia.getEntrada(),
                estancia.getSalida(),
                estancia.getNumeroHuespedes(),
                estancia.getTipoHabitacion() != null ? estancia.getTipoHabitacion().getId() : null,
                estancia.getTipoHabitacion() != null ? estancia.getTipoHabitacion().getNombre() : null,
                estancia.getPrecioPorNoche(),
                estancia.getTotalEstadia(),
                totalReserva
        );
    }
}
