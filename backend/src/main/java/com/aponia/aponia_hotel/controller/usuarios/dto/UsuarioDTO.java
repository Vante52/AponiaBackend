package com.aponia.aponia_hotel.controller.usuarios.dto;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioDTO(
        String id,
        String email,
        String rol,
        String nombreCompleto,
        String telefono,
        String cargo,
        BigDecimal salario,
        LocalDate fechaContratacion,
        LocalDateTime fechaRegistro
) {
    public static UsuarioDTO fromEntity(Usuario u) {
        ClientePerfil c = u.getClientePerfil();
        EmpleadoPerfil e = u.getEmpleadoPerfil();

        return new UsuarioDTO(
                u.getId(),
                u.getEmail(),
                u.getRol() != null ? u.getRol().name() : null,
                e != null ? e.getNombreCompleto() : (c != null ? c.getNombreCompleto() : null),
                e != null ? e.getTelefono() : (c != null ? c.getTelefono() : null),
                e != null ? e.getCargo() : null,
                e != null ? e.getSalario() : null,
                e != null ? e.getFechaContratacion() : null,
                c != null ? c.getFechaRegistro() : null
        );
    }
}
