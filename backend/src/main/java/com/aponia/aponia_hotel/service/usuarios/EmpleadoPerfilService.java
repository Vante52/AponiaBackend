package com.aponia.aponia_hotel.service.usuarios;

import java.util.List;
import java.util.Optional;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;


public interface EmpleadoPerfilService {

    List<EmpleadoPerfil> listar();

    EmpleadoPerfil crear(EmpleadoPerfil empleadoPerfil);

    Optional<EmpleadoPerfil> obtener(String id);

    EmpleadoPerfil actualizar(EmpleadoPerfil empleadoPerfil);

    void eliminar(String id);
}