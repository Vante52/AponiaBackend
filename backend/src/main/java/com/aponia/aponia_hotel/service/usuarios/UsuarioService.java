package com.aponia.aponia_hotel.service.usuarios;

import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;

public interface UsuarioService {
    List<Usuario> listar();
    Usuario crear(Usuario usuario);
    Optional<Usuario> obtener(String id);
    Usuario actualizar(Usuario usuario);
    void eliminar(String id);
    Optional<Usuario> findByEmail(String email);
}