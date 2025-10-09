
package com.aponia.aponia_hotel.controller.usuarios;

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

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8083")
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

    @GetMapping("/all")
    @Operation(summary = "Lista todos los usuarios")
    public List<Usuario> listar() { return usuarioService.listar(); }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene un usuario por ID")
    public Usuario findOne(@PathVariable String id) {
        return usuarioService.obtener(id).orElse(null);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtiene un usuario por email")
    public Usuario findByEmail(@PathVariable String email) {
        return usuarioService.findByEmail(email).orElse(null);
    }

    @PostMapping("/add")
    @Operation(summary = "Crea un usuario y vincula perfiles opcionales")
    public Usuario add(@RequestBody UsuarioCreateRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede estar vacía");
        }

        // construir entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setId(Optional.ofNullable(request.getId()).filter(id -> !id.isBlank())
                              .orElseGet(() -> UUID.randomUUID().toString()));
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Optional.ofNullable(request.getRol()).orElse(Usuario.UserRole.CLIENTE));

        Usuario creado = usuarioService.crear(usuario);

        // crear perfil de cliente si se incluye
        ClientePerfil clientePerfil = request.getClientePerfil();
        if (clientePerfil != null) {
            if (clientePerfilService.obtener(creado.getId()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El perfil de cliente ya existe para este usuario");
            }
            clientePerfil.setUsuarioId(creado.getId());
            clientePerfil.setUsuario(creado);
            creado.setClientePerfil(clientePerfilService.crear(clientePerfil));
        }

        // crear perfil de empleado si se incluye
        EmpleadoPerfil empleadoPerfil = request.getEmpleadoPerfil();
        if (empleadoPerfil != null) {
            if (empleadoPerfilService.obtener(creado.getId()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El perfil de empleado ya existe para este usuario");
            }
            empleadoPerfil.setUsuarioId(creado.getId());
            empleadoPerfil.setUsuario(creado);
            creado.setEmpleadoPerfil(empleadoPerfilService.crear(empleadoPerfil));
        }

        return creado;
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un usuario")
    public Usuario update(@RequestBody Usuario usuario) {
        return usuarioService.actualizar(usuario);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un usuario por ID")
    public void delete(@PathVariable String id) {
        clientePerfilService.obtener(id).ifPresent(p -> clientePerfilService.eliminar(id));
        empleadoPerfilService.obtener(id).ifPresent(p -> empleadoPerfilService.eliminar(id));
        usuarioService.eliminar(id);
    }

    @PostMapping("/{id}/perfil-cliente")
    @Operation(summary = "Crea/actualiza el perfil de CLIENTE para el usuario")
    public ClientePerfil upsertClientePerfil(@PathVariable String id, @RequestBody ClientePerfil body) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        ClientePerfil existente = clientePerfilService.obtener(id).orElse(null);
        if (existente != null) {
            // actualizar campos del perfil existente
            existente.setNombreCompleto(body.getNombreCompleto());
            existente.setTelefono(body.getTelefono());
            // si en el futuro añades más campos (dirección, etc.), actualízalos aquí
            return clientePerfilService.actualizar(existente);
        } else {
            body.setUsuarioId(id);
            body.setUsuario(usuario);
            return clientePerfilService.crear(body);
        }
    }

    @PostMapping("/{id}/perfil-empleado")
    @Operation(summary = "Crea/actualiza el perfil de EMPLEADO para el usuario")
    public EmpleadoPerfil upsertEmpleadoPerfil(@PathVariable String id, @RequestBody EmpleadoPerfil body) {
        Usuario usuario = usuarioService.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        EmpleadoPerfil existente = empleadoPerfilService.obtener(id).orElse(null);
        if (existente != null) {
            // actualizar campos del perfil existente
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
