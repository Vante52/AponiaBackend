package com.aponia.aponia_hotel.controller.usuarios.dto;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface UsuarioMapper {
    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    @Mapping(target = "rol", expression = "java(mapRol(usuario))")
    @Mapping(target = "nombreCompleto", expression = "java(resolveNombreCompleto(usuario))")
    @Mapping(target = "telefono", expression = "java(resolveTelefono(usuario))")
    @Mapping(target = "cargo", expression = "java(resolveCargo(usuario))")
    @Mapping(target = "salario", expression = "java(resolveSalario(usuario))")
    @Mapping(target = "fechaContratacion", expression = "java(resolveFechaContratacion(usuario))")
    @Mapping(target = "fechaRegistro", expression = "java(resolveFechaRegistro(usuario))")
    UsuarioDTO convert(Usuario usuario);

    default String mapRol(Usuario usuario) {
        return usuario.getRol() != null ? usuario.getRol().name() : null;
    }

    default ClientePerfil clientePerfil(Usuario usuario) {
        return usuario.getClientePerfil();
    }

    default EmpleadoPerfil empleadoPerfil(Usuario usuario) {
        return usuario.getEmpleadoPerfil();
    }

    default String resolveNombreCompleto(Usuario usuario) {
        EmpleadoPerfil empleado = empleadoPerfil(usuario);
        if (empleado != null) {
            return empleado.getNombreCompleto();
        }
        ClientePerfil cliente = clientePerfil(usuario);
        return cliente != null ? cliente.getNombreCompleto() : null;
    }

    default String resolveTelefono(Usuario usuario) {
        EmpleadoPerfil empleado = empleadoPerfil(usuario);
        if (empleado != null) {
            return empleado.getTelefono();
        }
        ClientePerfil cliente = clientePerfil(usuario);
        return cliente != null ? cliente.getTelefono() : null;
    }

    default String resolveCargo(Usuario usuario) {
        EmpleadoPerfil empleado = empleadoPerfil(usuario);
        return empleado != null ? empleado.getCargo() : null;
    }

    default BigDecimal resolveSalario(Usuario usuario) {
        EmpleadoPerfil empleado = empleadoPerfil(usuario);
        return empleado != null ? empleado.getSalario() : null;
    }

    default LocalDate resolveFechaContratacion(Usuario usuario) {
        EmpleadoPerfil empleado = empleadoPerfil(usuario);
        return empleado != null ? empleado.getFechaContratacion() : null;
    }

    default LocalDateTime resolveFechaRegistro(Usuario usuario) {
        ClientePerfil cliente = clientePerfil(usuario);
        return cliente != null ? cliente.getFechaRegistro() : null;
    }
}
