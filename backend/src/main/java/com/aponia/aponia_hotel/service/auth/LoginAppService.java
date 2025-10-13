package com.aponia.aponia_hotel.service.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

@Service
public class LoginAppService {

    private final UsuarioService usuarioService;

    public LoginAppService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * @param email correo electrónico del usuario
     * @param password contraseña
     * @return el Usuario autenticado si las credenciales son correctas
     */
    @Transactional(readOnly = true)
    public Usuario login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No existe un usuario con ese email.");
        }

        Usuario usuario = opt.get();
        if (!usuario.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }

        return usuario;
    }
}
