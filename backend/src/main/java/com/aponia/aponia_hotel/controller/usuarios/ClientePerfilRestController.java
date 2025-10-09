package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.ClientePerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes-perfil")
@CrossOrigin(origins = "http://localhost:8083")
@Tag(name = "Perfiles de Cliente", description = "CRUD de perfiles de clientes")
public class ClientePerfilRestController {

    private final ClientePerfilService service;
    private final UsuarioService usuarioService;

    public ClientePerfilRestController(ClientePerfilService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todos los perfiles de cliente")
    public List<ClientePerfil> listar() { return service.listar(); }

    @GetMapping("/find/{usuarioId}")
    @Operation(summary = "Obtiene un perfil de cliente por usuarioId")
    public ClientePerfil findOne(@PathVariable String usuarioId) {
        return service.obtener(usuarioId).orElse(null);
    }

    @PostMapping("/add")
    @Operation(summary = "Crea un perfil de cliente (MapsId)")
    public ClientePerfil add(@RequestParam String usuarioId, @RequestBody ClientePerfil perfil) {
        perfil.setUsuarioId(usuarioId);
        Usuario u = usuarioService.obtener(usuarioId).orElseThrow();
        perfil.setUsuario(u);
        return service.crear(perfil);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un perfil de cliente")
    public ClientePerfil update(@RequestBody ClientePerfil perfil) {
        return service.actualizar(perfil);
    }

    @DeleteMapping("/delete/{usuarioId}")
    @Operation(summary = "Elimina un perfil de cliente")
    public void delete(@PathVariable String usuarioId) {
        service.eliminar(usuarioId);
    }
}