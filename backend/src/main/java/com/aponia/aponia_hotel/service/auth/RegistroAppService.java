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
        var u = new Usuario();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setPasswordHash(password); // (plain por ahora)
        u.setRol(Usuario.UserRole.CLIENTE);
        u = usuarioService.crear(u); // queda managed en ESTA tx

        // 2) Perfil (dueño de la relación con @MapsId)
        var p = new ClientePerfil();
        p.setUsuario(u); // clave para @MapsId
        p.setNombreCompleto(nombreCompleto);
        p.setTelefono(telefono);
        clientePerfilService.crear(p);

        // (opcional) reflejar la relación en memoria
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
        var u = new Usuario();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setPasswordHash(password); // (plain por ahora)
        u.setRol(Usuario.UserRole.ADMIN);
        u = usuarioService.crear(u); // queda managed en ESTA tx

        // 2) Perfil (dueño de la relación con @MapsId)
        var p = new ClientePerfil();
        p.setUsuario(u); // clave para @MapsId
        p.setNombreCompleto(nombreCompleto);
        p.setTelefono(telefono);
        clientePerfilService.crear(p);

        // (opcional) reflejar la relación en memoria
        u.setClientePerfil(p);

        return u;
    }

    @Transactional
    public Usuario registrarEmpleado(String email, String password, String nombreCompleto,
            String telefono, String cargo, BigDecimal salario) {

        if (usuarioService.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        var u = new Usuario();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setPasswordHash(password);
        u.setRol(Usuario.UserRole.STAFF);
        u = usuarioService.crear(u);

        var e = new EmpleadoPerfil();
        e.setUsuario(u);
        e.setNombreCompleto(nombreCompleto);
        e.setTelefono(telefono);
        e.setCargo(cargo);
        e.setSalario(salario);
        empleadoPerfilService.crear(e);

        u.setEmpleadoPerfil(e);
        return u;
    }


}
