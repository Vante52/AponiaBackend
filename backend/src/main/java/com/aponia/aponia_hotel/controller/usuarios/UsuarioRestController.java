package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.*;
import com.aponia.aponia_hotel.service.usuarios.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8083")
@Tag(name = "Usuarios", description = "CRUD de usuarios + vinculación de perfiles")
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final ClientePerfilService clientePerfilService;
    private final EmpleadoPerfilService empleadoPerfilService;

    public UsuarioRestController(UsuarioService usuarioService,
                                 ClientePerfilService clientePerfilService,
                                 EmpleadoPerfilService empleadoPerfilService) {
        this.usuarioService = usuarioService;
        this.clientePerfilService = clientePerfilService;
        this.empleadoPerfilService = empleadoPerfilService;
    }

    // ===== Usuarios =====

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
    @Operation(summary = "Crea un usuario (sin perfil)")
    public Usuario add(@RequestBody Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isBlank()) {
            usuario.setId(UUID.randomUUID().toString());
        }
        if (usuario.getRol() == null) usuario.setRol(Usuario.UserRole.CLIENTE);
        return usuarioService.crear(usuario);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un usuario")
    public Usuario update(@RequestBody Usuario usuario) {
        return usuarioService.actualizar(usuario);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un usuario por ID")
    public void delete(@PathVariable String id) {
        // Nota: si el FK es mapsId, delete en cascada puede requerir borrar perfil antes
        clientePerfilService.obtener(id).ifPresent(p -> clientePerfilService.eliminar(id));
        empleadoPerfilService.obtener(id).ifPresent(p -> empleadoPerfilService.eliminar(id));
        usuarioService.eliminar(id);
    }

    // ===== Helper endpoints para vincular perfiles =====

    @PostMapping("/{id}/perfil-cliente")
    @Operation(summary = "Crea/actualiza el perfil de CLIENTE para el usuario")
    public ClientePerfil upsertClientePerfil(@PathVariable String id, @RequestBody ClientePerfil body) {
        body.setUsuarioId(id);
        Usuario u = usuarioService.obtener(id).orElseThrow();
        body.setUsuario(u);
        return (clientePerfilService.obtener(id).isPresent())
                ? clientePerfilService.actualizar(body)
                : clientePerfilService.crear(body);
    }

    @PostMapping("/{id}/perfil-empleado")
    @Operation(summary = "Crea/actualiza el perfil de EMPLEADO para el usuario")
    public EmpleadoPerfil upsertEmpleadoPerfil(@PathVariable String id, @RequestBody EmpleadoPerfil body) {
        body.setUsuarioId(id);
        Usuario u = usuarioService.obtener(id).orElseThrow();
        body.setUsuario(u);
        return (empleadoPerfilService.obtener(id).isPresent())
                ? empleadoPerfilService.actualizar(body)
                : empleadoPerfilService.crear(body);
    }
}