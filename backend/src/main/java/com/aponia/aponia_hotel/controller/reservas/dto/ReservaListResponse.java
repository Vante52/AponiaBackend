package com.aponia.aponia_hotel.controller.reservas.dto;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Estancia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaListResponse(
        String id,
        String codigo,
        String estado,
        LocalDate entrada,
        LocalDate salida,
        String clienteNombre,
        String clienteEmail,
        String notas
        ) {

    public static ReservaListResponse fromEntity(Reserva r) {
        LocalDate entrada = null;
        LocalDate salida = null;

        // Si la reserva tiene estancias, tomamos la primera
        if (r.getEstancias() != null && !r.getEstancias().isEmpty()) {
            Estancia estancia = r.getEstancias().get(0);
            entrada = estancia.getEntrada();
            salida = estancia.getSalida();
        }

        String clienteNombre = null;
        String clienteEmail = null;

        // Si el cliente existe, obtenemos sus datos desde su perfil
        if (r.getCliente() != null) {
            clienteEmail = r.getCliente().getEmail();

            if (r.getCliente().getClientePerfil() != null) {
                clienteNombre = r.getCliente().getClientePerfil().getNombreCompleto();
            } else if (r.getCliente().getEmpleadoPerfil() != null) {
                clienteNombre = r.getCliente().getEmpleadoPerfil().getNombreCompleto();
            }
        }

        return new ReservaListResponse(
                r.getId(),
                r.getCodigo(),
                r.getEstado().name(),
                entrada,
                salida,
                clienteNombre,
                clienteEmail,
                r.getNotas()
        );
    }
}
