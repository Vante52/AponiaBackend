package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.controller.usuarios.dto.UsuarioDTO;
import com.aponia.aponia_hotel.controller.usuarios.dto.UsuarioCreateRequest;
import com.aponia.aponia_hotel.entities.usuarios.*;
import com.aponia.aponia_hotel.service.usuarios.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Usuarios", description = "CRUD de usuarios + vinculación de perfiles")
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final ClientePerfilService clientePerfilService;
    private final EmpleadoPerfilService empleadoPerfilService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioRestController(UsuarioService usuarioService,
                                 ClientePerfilService clientePerfilService,
                                 EmpleadoPerfilService empleadoPerfilService,
                                 PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.clientePerfilService = clientePerfilService;
        this.empleadoPerfilService = empleadoPerfilService;
        this.passwordEncoder = passwordEncoder;
    }

    // === LISTADOS ===

    @GetMapping("/all")
    @Operation(summary = "Lista todos los usuarios con información básica de perfil")
    public List<UsuarioDTO> findAll() {
        return usuarioService.listar().stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene un usuario por ID (con su perfil si existe)")
    public UsuarioDTO findOne(@PathVariable String id) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return UsuarioDTO.fromEntity(usuario);
    }

    // === CREACIÓN ===

    @PostMapping("/add")
    @Operation(summary = "Crea un nuevo usuario (y su perfil si aplica)")
    public UsuarioDTO add(@RequestBody UsuarioCreateRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede estar vacía");
        }

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Optional.ofNullable(request.getRol()).orElse(Usuario.UserRole.CLIENTE));

        Usuario creado = usuarioService.crear(usuario);

        if (request.getClientePerfil() != null) {
            ClientePerfil c = request.getClientePerfil();
            c.setUsuarioId(creado.getId());
            c.setUsuario(creado);
            clientePerfilService.crear(c);
        }

        if (request.getEmpleadoPerfil() != null) {
            EmpleadoPerfil e = request.getEmpleadoPerfil();
            e.setUsuarioId(creado.getId());
            e.setUsuario(creado);
            empleadoPerfilService.crear(e);
        }

        return UsuarioDTO.fromEntity(creado);
    }

    // === ACTUALIZACIÓN ===

    @PutMapping("/update/{id}")
    @Operation(summary = "Actualiza datos básicos del usuario")
    public UsuarioDTO update(@PathVariable String id, @RequestBody UsuarioCreateRequest request) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            usuario.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        Usuario actualizado = usuarioService.actualizar(usuario);
        return UsuarioDTO.fromEntity(actualizado);
    }

    // === ELIMINACIÓN ===

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un usuario y su perfil asociado")
    public void delete(@PathVariable String id) {
        clientePerfilService.obtener(id).ifPresent(p -> clientePerfilService.eliminar(id));
        empleadoPerfilService.obtener(id).ifPresent(p -> empleadoPerfilService.eliminar(id));
        usuarioService.eliminar(id);
    }

    // === GESTIÓN DE PERFILES ===

    @PostMapping("/{id}/perfil-cliente")
    @Operation(summary = "Crea o actualiza el perfil de cliente")
    public ClientePerfil upsertClientePerfil(@PathVariable String id, @RequestBody ClientePerfil body) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        ClientePerfil existente = clientePerfilService.obtener(id).orElse(null);
        if (existente != null) {
            existente.setNombreCompleto(body.getNombreCompleto());
            existente.setTelefono(body.getTelefono());
            return clientePerfilService.actualizar(existente);
        } else {
            body.setUsuarioId(id);
            body.setUsuario(usuario);
            return clientePerfilService.crear(body);
        }
    }

    @PostMapping("/{id}/perfil-empleado")
    @Operation(summary = "Crea o actualiza el perfil de empleado")
    public EmpleadoPerfil upsertEmpleadoPerfil(@PathVariable String id, @RequestBody EmpleadoPerfil body) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        EmpleadoPerfil existente = empleadoPerfilService.obtener(id).orElse(null);
        if (existente != null) {
            existente.setNombreCompleto(body.getNombreCompleto());
            existente.setTelefono(body.getTelefono());
            existente.setCargo(body.getCargo());
            existente.setSalario(body.getSalario());
            existente.setFechaContratacion(body.getFechaContratacion());
            return empleadoPerfilService.actualizar(existente);
        } else {
            body.setUsuarioId(id);
            body.setUsuario(usuario);
            return empleadoPerfilService.crear(body);
        }
    }
}
