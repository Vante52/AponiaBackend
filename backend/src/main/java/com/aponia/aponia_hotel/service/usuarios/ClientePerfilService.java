package com.aponia.aponia_hotel.service.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import java.util.List;
import java.util.Optional;

public interface ClientePerfilService {
    /**
     * Lista todos los perfiles de cliente
     */
    List<ClientePerfil> listar();

    /**
     * Crea un nuevo perfil de cliente
     */
    ClientePerfil crear(ClientePerfil clientePerfil);

    /**
     * Obtiene un perfil de cliente por su ID
     */
    Optional<ClientePerfil> obtener(String id);

    /**
     * Actualiza un perfil de cliente existente
     */
    ClientePerfil actualizar(ClientePerfil clientePerfil);

    /**
     * Elimina un perfil de cliente por su ID
     */
    void eliminar(String id);

    // Métodos deprecados o no utilizados
    // @deprecated Usar obtener() en su lugar, el ID es el mismo que el del usuario
    // Optional<ClientePerfil> findByUsuarioId(String usuarioId);
}