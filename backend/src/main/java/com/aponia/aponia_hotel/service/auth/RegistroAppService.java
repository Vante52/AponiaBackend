package com.aponia.aponia_hotel.service.auth;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.ClientePerfilService;
import com.aponia.aponia_hotel.service.usuarios.EmpleadoPerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

@Service
public class RegistroAppService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClientePerfilService clientePerfilService;

    @Autowired
    private EmpleadoPerfilService empleadoPerfilService;

    @Transactional
    public Usuario registrarCliente(String email, String password, String nombreCompleto, String telefono) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (nombreCompleto == null || nombreCompleto.isBlank())
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        if (usuarioService.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        // 1) Usuario
        var u = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(password) // (plain por ahora)
                .rol(Usuario.UserRole.CLIENTE)
                .build();
        u = usuarioService.crear(u); // queda managed en ESTA tx

        // 2) Perfil (dueño de la relación con @MapsId)
        var p = ClientePerfil.builder()
                .usuario(u) // clave para @MapsId
                .nombreCompleto(nombreCompleto)
                .telefono(telefono)
                .build();
        clientePerfilService.crear(p);

        // reflejar la relación en memoria
        u.setClientePerfil(p);

        return u;
    }

    @Transactional
    public Usuario registrarAdmin(String email, String password, String nombreCompleto, String telefono) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (nombreCompleto == null || nombreCompleto.isBlank())
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        if (usuarioService.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        // 1) Usuario
        var u = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(password) // (plain por ahora)
                .rol(Usuario.UserRole.ADMIN)
                .build();
        u = usuarioService.crear(u); // queda managed en ESTA tx

        // 2) Perfil (dueño de la relación con @MapsId)
        var p = ClientePerfil.builder()
                .usuario(u) // clave para @MapsId
                .nombreCompleto(nombreCompleto)
                .telefono(telefono)
                .build();
        clientePerfilService.crear(p);

        // reflejar la relación en memoria
        u.setClientePerfil(p);

        return u;
    }

    @Transactional
    public Usuario registrarEmpleado(String email, String password, String nombreCompleto,
                                     String telefono, String cargo, BigDecimal salario) {

        if (usuarioService.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        var u = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(password)
                .rol(Usuario.UserRole.STAFF)
                .build();
        u = usuarioService.crear(u);

        var e = EmpleadoPerfil.builder()
                .usuario(u)
                .nombreCompleto(nombreCompleto)
                .telefono(telefono)
                .cargo(cargo)
                .salario(salario)
                .build();
        empleadoPerfilService.crear(e);

        u.setEmpleadoPerfil(e);
        return u;
    }
}
