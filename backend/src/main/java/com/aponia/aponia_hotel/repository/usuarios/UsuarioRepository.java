package com.aponia.aponia_hotel.repository.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndPasswordHash(String email, String passwordHash);
}