package com.aponia.aponia_hotel.controller.reservas.dto;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        TipoHabitacionDTO tipoHabitacion
    ) {
        public static EstanciaDTO fromEntity(Estancia e) {
            TipoHabitacionDTO tipoDto = null;
            if (e.getTipoHabitacion() != null) {
                List<ImagenDTO> imagenes = null;
                if (e.getTipoHabitacion().getImagenes() != null && !e.getTipoHabitacion().getImagenes().isEmpty()) {
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
            
            return new EstanciaDTO(
                e.getId(),
                e.getEntrada(),
                e.getSalida(),
                e.getNumeroHuespedes(),
                e.getPrecioPorNoche(),
                e.getTotalEstadia(),
                e.getCheckIn(),
                e.getCheckOut(),
                tipoDto
            );
        }
    }
    
    public record TipoHabitacionDTO(
        String id,
        String nombre,
        List<ImagenDTO> imagenes
    ) {}
    
    public record ImagenDTO(
        String id,
        String url
    ) {}
}