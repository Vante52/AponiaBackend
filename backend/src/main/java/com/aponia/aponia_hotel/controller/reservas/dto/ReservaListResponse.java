package com.aponia.aponia_hotel.controller.reservas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;

public record ReservaListResponse(
        String id,
        String codigo,
        String estado,
        String notas,
        String clienteNombre,
        String clienteEmail,
        List<EstanciaDTO> estancias
        ) {

    public static ReservaListResponse fromEntity(Reserva r) {
        String clienteNombre = null;
        String clienteEmail = null;

        if (r.getCliente() != null) {
            clienteEmail = r.getCliente().getEmail();
            if (r.getCliente().getClientePerfil() != null) {
                clienteNombre = r.getCliente().getClientePerfil().getNombreCompleto();
            } else if (r.getCliente().getEmpleadoPerfil() != null) {
                clienteNombre = r.getCliente().getEmpleadoPerfil().getNombreCompleto();
            }
        }

        List<EstanciaDTO> estancias = null;
        if (r.getEstancias() != null) {
            estancias = r.getEstancias().stream()
                    .map(EstanciaDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        return new ReservaListResponse(
                r.getId(),
                r.getCodigo(),
                r.getEstado().name(),
                r.getNotas(),
                clienteNombre,
                clienteEmail,
                estancias
        );
    }

    public record EstanciaDTO(
            String id,
            LocalDate entrada,
            LocalDate salida,
            Integer numeroHuespedes,
            BigDecimal precioPorNoche,
            BigDecimal totalEstadia,
            Boolean checkIn,
            Boolean checkOut,
            TipoHabitacionDTO tipoHabitacion,
            HabitacionAsignadaDTO habitacionAsignada // ← AÑADIR ESTO
            ) {

        public static EstanciaDTO fromEntity(Estancia e) {
            TipoHabitacionDTO tipoDto = null;
            if (e.getTipoHabitacion() != null) {
                // Cargar las imágenes por separado si es necesario
                List<ImagenDTO> imagenes = null;
                if (e.getTipoHabitacion().getImagenes() != null && !e.getTipoHabitacion().getImagenes().isEmpty()) {
                    // Hibernate.initialize para forzar la carga si es LAZY
                    Hibernate.initialize(e.getTipoHabitacion().getImagenes());
                    imagenes = e.getTipoHabitacion().getImagenes().stream()
                            .map(img -> new ImagenDTO(img.getId(), img.getUrl()))
                            .collect(Collectors.toList());
                }

                tipoDto = new TipoHabitacionDTO(
                        e.getTipoHabitacion().getId(),
                        e.getTipoHabitacion().getNombre(),
                        imagenes
                );
            }

            // Mapear la habitación asignada
            HabitacionAsignadaDTO habitacionDto = null;
            if (e.getHabitacionAsignada() != null) {
                habitacionDto = new HabitacionAsignadaDTO(
                        e.getHabitacionAsignada().getId(),
                        e.getHabitacionAsignada().getNumero()
                );
            }

            return new EstanciaDTO(
                    e.getId(),
                    e.getEntrada(),
                    e.getSalida(),
                    e.getNumeroHuespedes(),
                    e.getPrecioPorNoche(),
                    e.getTotalEstadia(),
                    e.getCheckIn(),
                    e.getCheckOut(),
                    tipoDto,
                    habitacionDto
            );
        }
    }

    public record TipoHabitacionDTO(
            String id,
            String nombre,
            List<ImagenDTO> imagenes
            ) {

    }

    public record HabitacionAsignadaDTO(
            String id,
            Integer numero
            ) {

    }

    public record ImagenDTO(
            String id,
            String url
            ) {

    }
}
